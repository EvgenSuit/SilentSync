package com.suit.feature.dndcalendar.presentation

sealed class DNDCalendarCriteriaInput {
    data class NameLike(val nameLike: String): DNDCalendarCriteriaInput()
}