package com.suit.silentsync.data

data class CalendarEventData(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val deleted: Boolean = false
)