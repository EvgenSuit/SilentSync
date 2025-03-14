package com.suit.dndCalendar.impl.data

import com.suit.dndcalendar.api.UpcomingEventData

data class CalendarEventData(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val deleted: Boolean = false
) {
    fun toUpcomingEvent() =
        UpcomingEventData(
            id = id,
            title = title,
            startTime = startTime,
            endTime = endTime
        )
}