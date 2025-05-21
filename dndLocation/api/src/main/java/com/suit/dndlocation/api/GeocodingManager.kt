package com.suit.dndlocation.api

interface GeocodingManager {
    suspend fun geocode(location: String): GeocodingResult
    suspend fun reverseGeocoding(): GeocodingResult
}