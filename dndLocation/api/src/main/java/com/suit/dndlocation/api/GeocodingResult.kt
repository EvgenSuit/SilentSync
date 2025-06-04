package com.suit.dndlocation.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResult(
    val features: List<Feature>?
)

@Serializable
data class Feature(
    val geometry: Geometry,
    val properties: Properties
)

@Serializable
data class Geometry(
    val coordinates: List<Double> // [longitude,latitude]
)

@Serializable
data class Properties(
    @SerialName("mapbox_id")
    val mapboxId: String,
    @SerialName("full_address")
    val fullAddress: String
)