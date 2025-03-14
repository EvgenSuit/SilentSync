package com.suit.feature.dndcalendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.dndcalendar.api.UpcomingEventsManager
import com.suit.feature.dndcalendar.R
import com.suit.utility.NoCalendarCriteriaFound
import com.suit.utility.ui.CustomResult
import com.suit.utility.ui.DNDCalendarUIEvent
import com.suit.utility.ui.UIText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DNDCalendarViewModel(
    private val dndCalendarScheduler: DNDCalendarScheduler,
    private val dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManager,
    private val upcomingEventsManager: UpcomingEventsManager,
    private val dispatcher: CoroutineDispatcher
): ViewModel() {
    private val upcomingEvents = upcomingEventsManager.upcomingEventsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), listOf())

    private val _uiState = MutableStateFlow(DNDCalendarUIState())
    val uiState = _uiState
        .onStart {
            fetchCriteria()
            onStartSync()
        }
        .combine(upcomingEvents) { uiState, events ->
            uiState.copy(
                upcomingEvents = events
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(3000), DNDCalendarUIState())


    private val _uiEvents = MutableSharedFlow<DNDCalendarUIEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    fun handleIntent(intent: DNDCalendarIntent) {
        when (intent) {
            DNDCalendarIntent.GetCriteria -> fetchCriteria()
            is DNDCalendarIntent.InputCriteria -> inputCriteria(intent.dndCalendarCriteriaInput)
            DNDCalendarIntent.Schedule -> schedule()

            is DNDCalendarIntent.ToggleDNDOn -> toggleDNDMode(true, intent.id, intent.set)
            is DNDCalendarIntent.ToggleDNDOff -> toggleDNDMode(false, intent.id, intent.set)
        }
    }

    private fun toggleDNDMode(dndOn: Boolean, id: Long, set: Boolean) {
        viewModelScope.launch {
            try {
                upcomingEventsManager.apply {
                    when (dndOn) {
                        true -> if (set) setDndOnToggle(id, upcomingEvents.value.firstOrNull { it.id == id }?.startTime ?: return@launch) else removeDndOnToggle(id)
                        false -> if (set) setDndOffToggle(id, upcomingEvents.value.firstOrNull { it.id == id }?.endTime ?: return@launch) else removeDndOffToggle(id)
                    }
                }
            } catch (e: Exception) {
                println(e)
            }
        }
    }

    private fun fetchCriteria() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(criteriaFetchResult = CustomResult.InProgress) }
            val criteria = dndScheduleCalendarCriteriaManager.getCriteria().first()
            _uiState.update { it.copy(criteria = criteria, criteriaFetchResult = if (criteria != null)
                CustomResult.Success else CustomResult.Error) }
        }
    }

    private fun inputCriteria(criteriaInput: DNDCalendarCriteriaInput) {
        val criteria = uiState.value.criteria

        val updatedCriteria =
            when (criteriaInput) {
                is DNDCalendarCriteriaInput.NameLike -> criteria?.copy(likeName = criteriaInput.nameLike)
                    ?: DNDScheduleCalendarCriteria(criteriaInput.nameLike)
            }
        _uiState.update {
            it.copy(criteria = updatedCriteria)
        }
    }

    private fun onStartSync() {
        viewModelScope.launch(dispatcher) {
            try {
                _uiState.update { it.copy(eventsSyncResult = CustomResult.InProgress) }
                dndCalendarScheduler.schedule()
                _uiState.update { it.copy(eventsSyncResult = CustomResult.Success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(eventsSyncResult = CustomResult.Error) }
                if (e !is NoCalendarCriteriaFound) {
                    _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(com.suit.utility.R.string.could_not_sync_events)))
                }
            }
        }
    }

    private fun schedule() {
        viewModelScope.launch(dispatcher) {
            try {
                uiState.value.criteria?.let {
                    _uiState.update { it.copy(eventsSyncResult = CustomResult.InProgress) }
                    dndScheduleCalendarCriteriaManager.changeCriteria(it)
                    dndCalendarScheduler.schedule()
                    _uiState.update { it.copy(eventsSyncResult = CustomResult.Success) }
                    _uiEvents.emit(DNDCalendarUIEvent.Unfocus)
                    _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(R.string.successfully_synced_events)))
                }
            } catch (e: Exception) {
                _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_update_criteria)))
                _uiState.update { it.copy(eventsSyncResult = CustomResult.Error) }
            }
        }
    }
}

data class DNDCalendarUIState(
    val eventsSyncResult: CustomResult = CustomResult.InProgress,
    val criteriaFetchResult: CustomResult = CustomResult.None,
    val upcomingEvents: List<UpcomingEventData> = listOf(),
    val criteria: DNDScheduleCalendarCriteria? = null
)