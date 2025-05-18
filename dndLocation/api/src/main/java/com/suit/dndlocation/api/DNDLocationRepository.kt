package com.suit.dndlocation.api

import androidx.room.TypeConverter
import kotlinx.coroutines.flow.Flow

class RadiusValue(val value: Int, val measurement: RadiusMeasurement)

sealed class RadiusMeasurement {
    data object Meters: RadiusMeasurement()
    data object Yards: RadiusMeasurement()
}

class RadiusMeasurementConverter {
    @TypeConverter
    fun fromRadiusMeasurement(radiusMeasurement: RadiusMeasurement): String {
        return when (radiusMeasurement) {
            RadiusMeasurement.Meters -> "Meters"
            RadiusMeasurement.Yards -> "Yards"
        }
    }
    @TypeConverter
    fun toRadiusMeasurement(value: String): RadiusMeasurement {
        return when (value) {
            "Meters" -> RadiusMeasurement.Meters
            "Yards" -> RadiusMeasurement.Yards
            else -> throw IllegalArgumentException("Invalid radius measurement: $value")
        }
    }
}

interface DNDLocationRepository {
    fun savedLocationsFlow(): Flow<List<SavedLocation>>
    suspend fun geocode(locationName: String): GeocodingResult
    fun startLocationService()
    suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                turnDNDOffUponExiting: Boolean,
                                radiusValue: RadiusValue)
}