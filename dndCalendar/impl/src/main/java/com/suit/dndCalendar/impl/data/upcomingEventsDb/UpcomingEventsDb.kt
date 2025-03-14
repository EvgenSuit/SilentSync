package com.suit.dndCalendar.impl.data.upcomingEventsDb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.suit.dndcalendar.api.UpcomingEventData

@Database(entities = [UpcomingEventData::class], version = 1)
internal abstract class UpcomingEventsDb: RoomDatabase() {
    abstract fun dao(): UpcomingEventsDAO
}