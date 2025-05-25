package com.suit.dndlocation.api

import kotlinx.coroutines.flow.Flow

interface LocationFeatureAvailabilityManager {
    suspend fun toggleFeatureAvailability(enabled: Boolean)
    fun isFeatureEnabled(): Flow<Boolean>
}