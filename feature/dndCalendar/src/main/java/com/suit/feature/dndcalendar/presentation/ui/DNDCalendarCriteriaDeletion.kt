package com.suit.feature.dndcalendar.presentation.ui

sealed class DNDCalendarCriteriaDeletion {
    data class NameLike(val nameLike: String): DNDCalendarCriteriaDeletion()
}