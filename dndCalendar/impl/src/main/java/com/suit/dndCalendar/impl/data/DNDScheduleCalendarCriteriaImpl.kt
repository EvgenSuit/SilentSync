package com.suit.dndCalendar.impl.data

import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaEntity
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DNDScheduleCalendarCriteriaManagerImpl(
    private val dndScheduleCalendarCriteriaDb: DNDScheduleCalendarCriteriaDb
): DNDScheduleCalendarCriteriaManager {

    override suspend fun changeCriteria(criteria: DNDScheduleCalendarCriteria) {
        dndScheduleCalendarCriteriaDb.dao().replaceCriteria(
            DNDScheduleCalendarCriteriaEntity(
                likeNames = criteria.likeNames,
                participants = criteria.attendees
            )
        )
    }

    override fun getCriteria(): Flow<DNDScheduleCalendarCriteria?> =
        dndScheduleCalendarCriteriaDb.dao().criteriaFlow().map { criteria ->
            criteria?.let {
                DNDScheduleCalendarCriteria(
                    likeNames = it.likeNames,
                    attendees = it.participants
                )
            }
        }
}