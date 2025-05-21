package com.suit.dndlocation.api

import androidx.room.Entity
import androidx.room.PrimaryKey

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
)
