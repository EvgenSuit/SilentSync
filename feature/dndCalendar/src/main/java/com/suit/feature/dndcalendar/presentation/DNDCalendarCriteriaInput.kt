package com.suit.feature.dndcalendar.presentation

sealed class DNDCalendarCriteriaInput {
    data class NameLike(val nameLike: String): DNDCalendarCriteriaInput()
    data class Participant(val participantName: String): DNDCalendarCriteriaInput()
}