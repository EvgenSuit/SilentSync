package com.suit.dndCalendar.impl.data.criteriaDb

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DNDScheduleCalendarCriteriaEntity::class], version = 1)
internal abstract class DNDScheduleCalendarCriteriaDb: RoomDatabase() {
    abstract fun dao(): DNDScheduleCalendarCriteriaDAO
}