package com.suit.dndlocation.api

import kotlinx.coroutines.flow.Flow

interface LocationsManager {
    fun fetchSavedLocations(): Flow<List<SavedLocation>>

}