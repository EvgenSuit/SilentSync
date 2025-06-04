package com.suit.dndlocation.api

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suit.dndlocation.api.RadiusMeasurement.Meters
import com.suit.dndlocation.api.RadiusMeasurement.Yards

@Entity
data class SavedLocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
    val radius: Double,
    val radiusMeasurement: RadiusMeasurement,
    val turnDNDOnUponEntering: Boolean,
    val turnDNDOffUponExiting: Boolean,

    val didEnter: Boolean = false,
    val didExit: Boolean = false
) {
    fun radiusValueMeters(): Double = when (radiusMeasurement) {
        is Meters -> radius
        is Yards -> radius * 0.9144
    }
}
