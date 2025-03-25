package com.suit.dndcalendar.api

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
@Keep
data class UpcomingEventData(
    @PrimaryKey
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    // scheduleDndOn and scheduleDndOff represent user-defined options
    val scheduleDndOn: Boolean = true,
    val scheduleDndOff: Boolean = true,
    val doesDndOffOverlap: Boolean = false
)
