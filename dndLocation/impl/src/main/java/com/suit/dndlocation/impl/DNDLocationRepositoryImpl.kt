package com.suit.dndlocation.impl

import android.content.Context
import android.content.Intent
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingManager
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.api.SavedLocation
import com.suit.dndlocation.impl.db.SavedLocationsDb

internal class DNDLocationRepositoryImpl(
    private val context: Context,
    private val savedLocationsDb: SavedLocationsDb,
    private val geocodingManager: GeocodingManager
): DNDLocationRepository {
    override suspend fun geocode(locationName: String): GeocodingResult {
        return geocodingManager.geocode(locationName)
    }

    override suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                         turnDNDOffUponExiting: Boolean,
                                         radiusValue: RadiusValue) {
        val properties = feature.properties
        val coordinates = feature.geometry.coordinates
        savedLocationsDb.savedLocationDao().insertLocation(
            SavedLocation(
                mapBoxId = properties.mapboxId,
                fullAddress = properties.fullAddress,
                longitude = coordinates[0],
                latitude = coordinates[1],
                radiusMeters = convertRadiusToMeters(radiusValue),
                turnDNDOnUponEntering = turnDNDOnUponEntering,
                turnDNDOffUponExiting = turnDNDOffUponExiting
            )
        )
       startLocationService()
    }

    private fun startLocationService() {
        val locationServiceIntent = Intent(context, LocationService::class.java)
        context.stopService(locationServiceIntent);
        context.startForegroundService(locationServiceIntent)
    }

    private fun convertRadiusToMeters(radiusValue: RadiusValue): Double =
        when (radiusValue.measurement) {
            RadiusMeasurement.Yards -> radiusValue.value * 0.9144
            RadiusMeasurement.Meters -> radiusValue.value.toDouble()
        }
}