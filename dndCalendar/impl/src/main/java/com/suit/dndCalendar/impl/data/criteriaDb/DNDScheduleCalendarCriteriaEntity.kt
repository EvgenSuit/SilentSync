package com.suit.dndCalendar.impl.data.criteriaDb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DNDScheduleCalendarCriteriaEntity(
    @PrimaryKey val id: Long = 1,
    val likeNames: List<String>
)
