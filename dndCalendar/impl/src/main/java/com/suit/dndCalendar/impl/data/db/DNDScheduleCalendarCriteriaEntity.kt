package com.suit.dndCalendar.impl.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DNDScheduleCalendarCriteriaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 1,
    val likeName: String
)
