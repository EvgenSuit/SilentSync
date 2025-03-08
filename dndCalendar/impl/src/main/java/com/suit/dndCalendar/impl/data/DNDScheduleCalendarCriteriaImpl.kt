package com.suit.dndCalendar.impl.data

import com.suit.dndCalendar.impl.data.db.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.db.DNDScheduleCalendarCriteriaEntity
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DNDScheduleCalendarCriteriaManagerImpl(
    private val db: DNDScheduleCalendarCriteriaDb
): DNDScheduleCalendarCriteriaManager {

    override suspend fun changeCriteria(criteria: DNDScheduleCalendarCriteria) {
        db.dao().replaceCriteria(
            DNDScheduleCalendarCriteriaEntity(
                likeName = criteria.likeName
            )
        )
    }

    override fun getCriteria(): Flow<DNDScheduleCalendarCriteria?> =
        db.dao().criteriaFlow().map { criteria ->
            criteria?.let {
                DNDScheduleCalendarCriteria(
                    likeName = it.likeName
                )
            }
        }
}