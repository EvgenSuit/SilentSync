package com.suit.dndcalendar.api

data class DNDScheduleCalendarCriteria(
    val likeNames: List<String>
) {
    fun isEmpty() = likeNames.isEmpty()
}