package com.suit.dndCalendar.impl.koin

import androidx.room.Room
import com.suit.dndCalendar.impl.data.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.data.DNDCalendarSchedulerImpl
import com.suit.dndCalendar.impl.data.db.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.DNDScheduleCalendarCriteriaManagerImpl
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.time.Clock

val dndCalendarImplKoinModule = module {
    single { Clock.systemDefaultZone() }
    factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    single<CalendarEventChecker> { CalendarEventCheckerImpl(
        contentResolver = androidContext().contentResolver,
        clock = get()) }
    single<DNDScheduleCalendarCriteriaManager> {
        DNDScheduleCalendarCriteriaManagerImpl(
            db = Room.databaseBuilder(
                androidContext(),
                DNDScheduleCalendarCriteriaDb::class.java,
                "dnd-schedule-calendar-db"
            ).build()
        )
    }
    single<DNDCalendarScheduler> { DNDCalendarSchedulerImpl(clock = get(),
        context = androidContext(),
        dndScheduleCalendarCriteriaManager = get()) }
}