package com.suit.feature.dndlocation.presentation

import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.impl.CurrentLocation
import com.suit.feature.dndlocation.presentation.ui.DNDLocationIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DNDLocationViewModel(
    private val dndLocationRepository: DNDLocationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DNDLocationUIState())
    val uiState = _uiState.asStateFlow()

    val locationFlow = /*flow<Location?> {
            emit(Location("").apply {
                latitude = 51.08793
                longitude = 17.011963
            })
            delay(2000)
            emit(Location("").apply {
                latitude = 51.08735
                longitude = 17.011945
            })
            delay(2000)
            emit(Location("").apply {
                latitude = 51.08773
                longitude = 17.011963
            })
            delay(2000)
            emit(Location("").apply {
                latitude = 51.08650
                longitude = 17.011919
            })
            delay(2000)
            emit(Location("").apply {
                latitude = 51.08793
                longitude = 17.011963
            })
        }*/
        CurrentLocation.location
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val savedLocations = dndLocationRepository.savedLocationsFlow()
        .map { locations ->
            println("Locations: $locations")
            // render locations with higher radius first
            locations.sortedByDescending { it.radius }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun handleIntent(intent: DNDLocationIntent) {
        when (intent) {
            is DNDLocationIntent.StartService -> dndLocationRepository.startLocationService(highAccuracyMode = intent.highAccuracyMode)
            is DNDLocationIntent.LocationInput -> onLocationInput(intent.location)
            is DNDLocationIntent.ConfirmLocation -> confirmLocation(
                intent.feature, intent.turnDNDOnUponEntering, intent.turnDNDOffUponExiting, intent.radiusValue
            )
            is DNDLocationIntent.DeleteLocation -> deleteLocation(intent.id)
        }
    }

    private fun onLocationInput(location: String) {
        if (location.contains(';')) return
        val formattedLocation = location
            .take(256)
        _uiState.update { it.copy(locationInput = formattedLocation) }
        viewModelScope.launch {
            delay(820)
            try {
                val geocodingResult = dndLocationRepository.geocode(formattedLocation)
                _uiState.update { it.copy(geocodingResult = geocodingResult) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun deleteLocation(id: Long) {
        viewModelScope.launch {
            try {
                dndLocationRepository.deleteLocation(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                turnDNDOffUponExiting: Boolean,
                                radiusValue: RadiusValue) {
        viewModelScope.launch {
            dndLocationRepository.confirmLocation(
                feature,
                turnDNDOnUponEntering,
                turnDNDOffUponExiting,
                radiusValue
            )
        }
    }

}

data class DNDLocationUIState(
    val locationInput: String = "",
    val geocodingResult: GeocodingResult? = null
)