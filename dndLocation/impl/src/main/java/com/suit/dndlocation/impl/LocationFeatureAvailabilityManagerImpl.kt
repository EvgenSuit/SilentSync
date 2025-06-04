package com.suit.dndlocation.impl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.suit.dndlocation.api.LocationFeatureAvailabilityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.locationFeatureAvailabilityDatastore by preferencesDataStore("location_feature_availability")
private val isLocationFeatureEnabled = booleanPreferencesKey("is_location_feature_enabled")
internal class LocationFeatureAvailabilityManagerImpl(
    private val dataStore: DataStore<Preferences>
): LocationFeatureAvailabilityManager {
    override suspend fun toggleFeatureAvailability(enabled: Boolean) {
        dataStore.edit { store ->
            store[isLocationFeatureEnabled] = enabled
        }
    }
    override fun isFeatureEnabled(): Flow<Boolean> =
        dataStore.data.map { it[isLocationFeatureEnabled] != false }
}