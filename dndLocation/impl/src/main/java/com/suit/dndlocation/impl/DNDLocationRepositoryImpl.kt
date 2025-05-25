package com.suit.dndlocation.impl

import android.content.Context
import android.content.Intent
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

internal class DNDLocationRepositoryImpl(
    private val context: Context,
    private val savedLocationsDb: SavedLocationsDb,
    private val locationFeatureAvailabilityManager: LocationFeatureAvailabilityManager,
    private val geocodingManager: GeocodingManager
): DNDLocationRepository {
    override fun savedLocationsFlow(): Flow<List<SavedLocation>> {
        return savedLocationsDb.savedLocationDao().fetchLocations()
    }

    override suspend fun toggleFeatureAvailability(enabled: Boolean) {
        locationFeatureAvailabilityManager.toggleFeatureAvailability(enabled)
        toggleLocationService(true, enabled)
    }

    override fun isFeatureEnabled(): Flow<Boolean> = locationFeatureAvailabilityManager.isFeatureEnabled()

    override suspend fun geocode(locationName: String): GeocodingResult {
        return geocodingManager.geocode(locationName)
    }

    override suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                         turnDNDOffUponExiting: Boolean,
                                         radiusValue: RadiusValue) {
        val properties = feature.properties
        val coordinates = feature.geometry.coordinates
        savedLocationsDb.savedLocationDao().apply {
            if (locationExists(coordinates[0], coordinates[1])) {
                getLocationId(coordinates[0], coordinates[1])?.let { existingId ->
                    deleteLocation(existingId)
                }
            }
            insertLocation(
                SavedLocation(
                    fullAddress = properties.fullAddress,
                    longitude = coordinates[0],
                    latitude = coordinates[1],
                    radius = radiusValue.value.toDouble(),
                    radiusMeasurement = radiusValue.measurement,
                    turnDNDOnUponEntering = turnDNDOnUponEntering,
                    turnDNDOffUponExiting = turnDNDOffUponExiting
                )
            )
        }
    }

    override suspend fun deleteLocation(id: Long) {
        savedLocationsDb.savedLocationDao().deleteLocation(id)
    }

    override suspend fun toggleLocationService(highAccuracyMode: Boolean, isEnabled: Boolean) {
        val locationServiceIntent = Intent(context, LocationService::class.java).apply {
            putExtra("HIGH_ACCURACY_MODE", highAccuracyMode)
        }
        // start a fresh new service
        context.stopService(locationServiceIntent)
        if (isEnabled) context.startForegroundService(locationServiceIntent)
    }
}