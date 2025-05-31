package com.suit.feature.dndlocation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suit.dndlocation.api.DNDLocationRepository
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.impl.CurrentLocation
import com.suit.feature.dndlocation.R
import com.suit.feature.dndlocation.presentation.ui.DNDLocationIntent
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.ui.DNDLocationUIEvent
import com.suit.utility.ui.UIText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DNDLocationViewModel(
    private val dndLocationRepository: DNDLocationRepository,
    private val dispatcher: CoroutineDispatcher,
    private val analytics: SilentSyncAnalytics
): ViewModel() {
    private val _uiState = MutableStateFlow(DNDLocationUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DNDLocationUIEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

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
            // render locations with higher radius first
            locations.sortedByDescending { it.radius }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val isLocationFeatureEnabled = dndLocationRepository.isFeatureEnabled()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun handleIntent(intent: DNDLocationIntent) {
        when (intent) {
            is DNDLocationIntent.ToggleService -> toggleLocationServiceAccuracy(intent.highAccuracyMode)
            is DNDLocationIntent.ToggleLocationFeatureAvailability -> toggleFeatureAvailability(intent.enable)
            is DNDLocationIntent.LocationInput -> onLocationInput(intent.location)
            is DNDLocationIntent.ConfirmLocation -> confirmLocation(
                intent.feature, intent.turnDNDOnUponEntering, intent.turnDNDOffUponExiting, intent.radiusValue
            )
            is DNDLocationIntent.DeleteLocation -> deleteLocation(intent.id)
        }
    }
    private fun toggleLocationServiceAccuracy(highAccuracyMode: Boolean) {
        viewModelScope.launch(dispatcher) {
            try {
                dndLocationRepository.toggleLocationService(highAccuracyMode, dndLocationRepository.isFeatureEnabled().first())
            } catch (e: Exception) {
                analytics.recordException(e)
            }
        }
    }

    private fun toggleFeatureAvailability(enable: Boolean) {
        viewModelScope.launch(dispatcher) {
            try {
                dndLocationRepository.toggleFeatureAvailability(enable)
            } catch (e: Exception) {
                _uiEvent.emit(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_toggle_feature_availability)))
                analytics.recordException(e)
            }
        }
    }

    private fun onLocationInput(location: String) {
        if (location.contains(';')) return
        val formattedLocation = location
            .take(256)
        _uiState.update { it.copy(locationInput = formattedLocation) }
        viewModelScope.launch(dispatcher) {
            delay(820)
            try {
                val geocodingResult = dndLocationRepository.geocode(formattedLocation)
                _uiState.update { it.copy(geocodingResult = geocodingResult) }
            } catch (e: Exception) {
                _uiEvent.emit(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_fetch_locations)))
                analytics.recordException(e)
            }
        }
    }
    private fun deleteLocation(id: Long) {
        viewModelScope.launch(dispatcher) {
            try {
                dndLocationRepository.deleteLocation(id)
            } catch (e: Exception) {
                _uiEvent.emit(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_delete_location)))
                analytics.recordException(e)
            }
        }
    }

    private fun confirmLocation(feature: Feature, turnDNDOnUponEntering: Boolean,
                                turnDNDOffUponExiting: Boolean,
                                radiusValue: RadiusValue) {
        viewModelScope.launch(dispatcher) {
            try {
                dndLocationRepository.confirmLocation(
                    feature,
                    turnDNDOnUponEntering,
                    turnDNDOffUponExiting,
                    radiusValue
                )
            } catch (e: Exception) {
                _uiEvent.emit(DNDLocationUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_add_location)))
                analytics.recordException(e)
            }
        }
    }

}

data class DNDLocationUIState(
    val locationInput: String = "",
    val geocodingResult: GeocodingResult? = null
)