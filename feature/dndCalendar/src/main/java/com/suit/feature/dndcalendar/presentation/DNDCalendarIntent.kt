package com.suit.feature.dndcalendar.presentation

sealed class DNDCalendarIntent {
    data object ScheduleDND: DNDCalendarIntent()
}