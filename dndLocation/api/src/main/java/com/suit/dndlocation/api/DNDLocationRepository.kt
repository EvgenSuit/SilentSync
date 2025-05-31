package com.suit.dndlocation.api

import androidx.room.TypeConverter
import com.suit.dndlocation.api.RadiusMeasurement.Meters
import com.suit.dndlocation.api.RadiusMeasurement.Yards
import kotlinx.coroutines.flow.Flow
import kotlin.text.toDouble

class RadiusValue(val value: Int, val measurement: RadiusMeasurement) {
    fun toValue(): Double = when (measurement) {
        is Meters -> value.toDouble()
        is Yards -> value.toDouble() * 0.9144
    }
}

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

    suspend fun toggleFeatureAvailability(enabled: Boolean)
    fun isFeatureEnabled(): Flow<Boolean>

    suspend fun geocode(locationName: String): GeocodingResult
    suspend fun toggleLocationService(highAccuracyMode: Boolean, isEnabled: Boolean)
    suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                turnDNDOffUponExiting: Boolean,
                                radiusValue: RadiusValue)
    suspend fun deleteLocation(id: Long)
}