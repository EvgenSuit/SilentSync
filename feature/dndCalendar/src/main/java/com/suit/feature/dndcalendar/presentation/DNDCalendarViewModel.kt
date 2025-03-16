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
            is DNDCalendarIntent.InputCriteria -> inputCriteriaPreview(intent.dndCalendarCriteriaInput)
            is DNDCalendarIntent.DeleteCriteria -> deleteCriteriaPreview(intent.dndCalendarCriteriaDeletion)
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
                _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(R.string.could_not_toggle_dnd_mode)))
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

    private fun inputCriteriaPreview(criteriaInput: DNDCalendarCriteriaInput) {
        val criteria = uiState.value.criteria

        val updatedCriteria =
            when (criteriaInput) {
                is DNDCalendarCriteriaInput.NameLike -> criteria?.copy(likeNames = criteria.likeNames + criteriaInput.nameLike.trim())
                    ?: DNDScheduleCalendarCriteria(
                        likeNames = listOf(criteriaInput.nameLike),
                        attendees = listOf())
                is DNDCalendarCriteriaInput.Participant -> criteria?.copy(
                    attendees = criteria.attendees + criteriaInput.participantName.trim()
                ) ?: DNDScheduleCalendarCriteria(
                    likeNames = listOf(),
                    attendees = listOf(criteriaInput.participantName))
            }
        _uiState.update {
            it.copy(criteria = updatedCriteria)
        }
    }

    private fun deleteCriteriaPreview(criteriaDeletion: DNDCalendarCriteriaDeletion) {
        val criteria = uiState.value.criteria

        val updatedCriteria = when (criteriaDeletion) {
            is DNDCalendarCriteriaDeletion.NameLike ->
                criteria?.copy(likeNames = criteria.likeNames.filterNot { it == criteriaDeletion.nameLike })
            is DNDCalendarCriteriaDeletion.Participant ->
                criteria?.copy(attendees = criteria.attendees.filterNot { it == criteriaDeletion.participantName })
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
                println(e)
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
                _uiState.update { it.copy(eventsSyncResult = CustomResult.InProgress) }
                uiState.value.criteria?.let {
                    dndScheduleCalendarCriteriaManager.changeCriteria(it)
                }
                dndCalendarScheduler.schedule()
                _uiState.update { it.copy(eventsSyncResult = CustomResult.Success) }
                _uiEvents.emit(DNDCalendarUIEvent.Unfocus)
                _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(R.string.successfully_synced_events)))
            } catch (e: Exception) {
                println(e)
                _uiEvents.emit(DNDCalendarUIEvent.ShowSnackbar(UIText.StringResource(com.suit.utility.R.string.could_not_sync_events)))
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