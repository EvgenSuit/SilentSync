package com.suit.dndlocation.impl

import android.location.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object CurrentLocation {
    private var _location = MutableStateFlow<Location?>(null)
    val location = _location.asStateFlow()

    fun updateLocation(newLocation: Location) {
        _location.value = newLocation
    }
}