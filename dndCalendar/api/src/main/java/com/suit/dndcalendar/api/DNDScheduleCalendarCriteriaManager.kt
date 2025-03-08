package com.suit.dndcalendar.api

import kotlinx.coroutines.flow.Flow

interface DNDScheduleCalendarCriteriaManager {
    suspend fun changeCriteria(criteria: DNDScheduleCalendarCriteria)
    fun getCriteria(): Flow<DNDScheduleCalendarCriteria?>
}