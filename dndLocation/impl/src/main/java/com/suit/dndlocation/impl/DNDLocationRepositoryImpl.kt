package com.suit.dndlocation.impl

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingManager
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.LocationFeatureAvailabilityManager
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.api.SavedLocation
import com.suit.dndlocation.impl.db.SavedLocationsDb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.koin.androidContext

internal class DNDLocationRepositoryImpl(
    private val context: Context,
    private val savedLocationsDb: SavedLocationsDb,
    private val locationFeatureAvailabilityManager: LocationFeatureAvailabilityManager,
    private val geocodingManager: GeocodingManager,
    private val geofencingClient: GeofencingClient
): DNDLocationRepository {
    override fun savedLocationsFlow(): Flow<List<SavedLocation>> {
        return savedLocationsDb.savedLocationDao().fetchLocations()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun toggleFeatureAvailability(enabled: Boolean) {
        locationFeatureAvailabilityManager.toggleFeatureAvailability(enabled)
        toggleLocationService(true, enabled)
    }

    override fun isFeatureEnabled(): Flow<Boolean> = locationFeatureAvailabilityManager.isFeatureEnabled()

    override suspend fun geocode(locationName: String): GeocodingResult {
        return geocodingManager.geocode(locationName)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                         turnDNDOffUponExiting: Boolean,
                                         radiusValue: RadiusValue) {
        val properties = feature.properties
        val coordinates = feature.geometry.coordinates
        val savedLocation = SavedLocation(
            fullAddress = properties.fullAddress,
            longitude = coordinates[0],
            latitude = coordinates[1],
            radius = radiusValue.value.toDouble(),
            radiusMeasurement = radiusValue.measurement,
            turnDNDOnUponEntering = turnDNDOnUponEntering,
            turnDNDOffUponExiting = turnDNDOffUponExiting
        )
        savedLocationsDb.savedLocationDao().apply {
            if (locationExists(savedLocation.longitude, savedLocation.latitude)) {
                getLocationId(savedLocation.longitude, savedLocation.latitude)?.let { existingId ->
                    deleteLocation(existingId)
                }
            }
            insertLocation(savedLocation)
        }
        addGeofence(savedLocation)
    }

    override suspend fun deleteLocation(id: Long) {
        removeGeofence(id)
        savedLocationsDb.savedLocationDao().deleteLocation(id)
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun toggleLocationService(highAccuracyMode: Boolean, isEnabled: Boolean) {
        val locationServiceIntent = Intent(context, LocationService::class.java)
        context.stopService(locationServiceIntent)
        if (isEnabled) {
            context.startForegroundService(locationServiceIntent.apply {
                putExtra("HIGH_ACCURACY_MODE", highAccuracyMode)
            })
            savedLocationsDb.savedLocationDao().fetchLocations().first().forEach { location ->
                addGeofence(location)
            }
        } else {
            removeGeofence(*savedLocationsDb.savedLocationDao().fetchLocations().first().map { it.id }.toLongArray())
            savedLocationsDb.savedLocationDao().resetAllZoneStatuses()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun addGeofence(savedLocation: SavedLocation) {
        geofencingClient.addGeofences(
            GeofencingRequest.Builder().apply {
                setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                addGeofences(listOf(Geofence.Builder()
                    .setRequestId(savedLocationsDb.savedLocationDao().getLocationId(savedLocation.longitude, savedLocation.latitude).toString())
                    .setCircularRegion(savedLocation.latitude, savedLocation.longitude, savedLocation.radiusValueMeters().toFloat())
                    .build()))
            }.build(),
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, GeofenceBroadcastReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        ).await()
    }
    private suspend fun removeGeofence(vararg ids: Long) {
        geofencingClient.removeGeofences(ids.map { it.toString() }).await()
    }
}