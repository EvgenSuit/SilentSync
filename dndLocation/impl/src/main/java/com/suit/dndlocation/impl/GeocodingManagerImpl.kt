package com.suit.dndlocation.impl

import SilentSync.dndLocation.impl.BuildConfig
import com.suit.dndlocation.api.GeocodingManager
import com.suit.dndlocation.api.GeocodingResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class GeocodingManagerImpl(
    engine: HttpClientEngine
): GeocodingManager {
    private val client = HttpClient(engine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url("https://api.mapbox.com/search/geocode/v6/")
        }
    }


    override suspend fun geocode(location: String): GeocodingResult {
        val request = client.get("forward") {
            url {
                parameters.append("q", location)
                parameters.append("access_token", BuildConfig.MAPBOX_ACCESS_TOKEN)
            }
        }.body<GeocodingResult?>()
        return request!!
    }

    override suspend fun reverseGeocoding(): GeocodingResult {
        TODO("Not yet implemented")
    }

}