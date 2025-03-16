package com.suit.dndCalendar.impl.data.criteriaDb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [DNDScheduleCalendarCriteriaEntity::class], version = 1)
@TypeConverters(CriteriaConverter::class)
internal abstract class DNDScheduleCalendarCriteriaDb: RoomDatabase() {
    abstract fun dao(): DNDScheduleCalendarCriteriaDAO
}