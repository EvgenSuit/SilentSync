package com.suit.feature.dndcalendar.presentation

sealed class DNDCalendarCriteriaDeletion {
    data class NameLike(val nameLike: String): DNDCalendarCriteriaDeletion()
    data class Participant(val participantName: String): DNDCalendarCriteriaDeletion()
}