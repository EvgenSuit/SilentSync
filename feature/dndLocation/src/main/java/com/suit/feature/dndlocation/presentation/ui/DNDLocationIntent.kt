package com.suit.feature.dndlocation.presentation.ui

import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.RadiusValue

sealed class DNDLocationIntent {
    data class ToggleService(val highAccuracyMode: Boolean): DNDLocationIntent()
    data class LocationInput(val location: String): DNDLocationIntent()
    data class ConfirmLocation(val feature: Feature, val turnDNDOnUponEntering: Boolean, val turnDNDOffUponExiting: Boolean, val radiusValue: RadiusValue): DNDLocationIntent()
    data class DeleteLocation(val id: Long): DNDLocationIntent()
    data class ToggleLocationFeatureAvailability(val enable: Boolean): DNDLocationIntent()
}