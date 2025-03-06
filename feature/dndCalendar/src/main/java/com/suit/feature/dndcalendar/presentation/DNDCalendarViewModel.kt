package com.suit.feature.dndcalendar.presentation

import androidx.lifecycle.ViewModel
import com.suit.dndcalendar.api.DNDCalendarScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DNDCalendarViewModel(
    private val dndCalendarScheduler: DNDCalendarScheduler
): ViewModel() {
    private val _uiState = MutableStateFlow(DNDCalendarUIState())
    val uiState = _uiState.asStateFlow()

    fun handleIntent(intent: DNDCalendarIntent) {
        when (intent) {
            DNDCalendarIntent.ScheduleDND -> schedule()
        }
    }

    private fun schedule() {
        try {
            dndCalendarScheduler.schedule()
            _uiState.update { it.copy(areEventsSynced = true) }
        } catch (e: Exception) {

        }
    }
}

data class DNDCalendarUIState(
    val areEventsSynced: Boolean = false
)