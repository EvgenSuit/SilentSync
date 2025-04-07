package com.suit.feature.dndlocation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.RadiusValue
import com.suit.feature.dndlocation.presentation.ui.DNDLocationIntent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DNDLocationViewModel(
    private val dndLocationRepository: DNDLocationRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DNDLocationUIState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: DNDLocationIntent) {
        when (intent) {
            is DNDLocationIntent.LocationInput -> onLocationInput(intent.location)
            is DNDLocationIntent.ConfirmLocation -> confirmLocation(
                intent.feature, intent.turnDNDOnUponEntering, intent.turnDNDOffUponExiting, intent.radiusValue
            )
        }
    }

    private fun onLocationInput(location: String) {
        if (location.contains(';')) return
        val formattedLocation = location
            .take(256)
        _uiState.update { it.copy(locationInput = formattedLocation) }
        viewModelScope.launch {
            delay(700)
            val geocodingResult = dndLocationRepository.geocode(formattedLocation)
            _uiState.update { it.copy(geocodingResult = geocodingResult) }
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