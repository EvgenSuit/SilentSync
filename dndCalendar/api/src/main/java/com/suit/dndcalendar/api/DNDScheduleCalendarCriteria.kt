package com.suit.dndcalendar.api

data class DNDScheduleCalendarCriteria(
    val likeNames: List<String>,
    val attendees: List<String>
) {
    fun isEmpty() = likeNames.isEmpty()
            && attendees.isEmpty()
}