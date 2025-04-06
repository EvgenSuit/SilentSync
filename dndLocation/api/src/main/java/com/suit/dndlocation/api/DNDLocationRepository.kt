package com.suit.dndlocation.api

class RadiusValue(val value: Int, val measurement: RadiusMeasurement)

sealed class RadiusMeasurement {
    data object Meters: RadiusMeasurement()
    data object Yards: RadiusMeasurement()
}

interface DNDLocationRepository {
    suspend fun geocode(locationName: String): GeocodingResult
    suspend fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                turnDNDOffUponExiting: Boolean,
                                radiusValue: RadiusValue)
}