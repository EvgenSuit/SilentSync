package com.suit.feature.dndcalendar.presentation

sealed class DNDCalendarIntent {
    data object GetCriteria: DNDCalendarIntent()
    data class InputCriteria(val dndCalendarCriteriaInput: DNDCalendarCriteriaInput): DNDCalendarIntent()
    data object Schedule: DNDCalendarIntent()

    data class ToggleDNDOn(val id: Long, val set: Boolean): DNDCalendarIntent()
    data class ToggleDNDOff(val id: Long, val set: Boolean): DNDCalendarIntent()
}