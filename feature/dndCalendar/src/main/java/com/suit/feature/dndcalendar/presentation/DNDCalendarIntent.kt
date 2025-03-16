package com.suit.feature.dndcalendar.presentation

import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarCriteriaDeletion

sealed class DNDCalendarIntent {
    data object GetCriteria: DNDCalendarIntent()
    data class InputCriteria(val dndCalendarCriteriaInput: DNDCalendarCriteriaInput): DNDCalendarIntent()
    data class DeleteCriteria(val dndCalendarCriteriaDeletion: DNDCalendarCriteriaDeletion): DNDCalendarIntent()
    data object Schedule: DNDCalendarIntent()

    data class ToggleDNDOn(val id: Long, val set: Boolean): DNDCalendarIntent()
    data class ToggleDNDOff(val id: Long, val set: Boolean): DNDCalendarIntent()
}