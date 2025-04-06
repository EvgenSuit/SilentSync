package com.suit.dndlocation.api

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SavedLocation(
    @PrimaryKey
    val mapBoxId: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
    val radiusMeters: Double,
    val turnDNDOnUponEntering: Boolean,
    val turnDNDOffUponExiting: Boolean
)
