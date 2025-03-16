package com.suit.dndCalendar.impl.koin

import androidx.room.Room
import com.suit.dndCalendar.impl.data.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.data.DNDCalendarSchedulerImpl
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.DNDScheduleCalendarCriteriaManagerImpl
import com.suit.dndCalendar.impl.data.UpcomingEventsManagerImpl
import com.suit.dndCalendar.impl.data.upcomingEventsDb.UpcomingEventsDb
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import com.suit.dndcalendar.api.UpcomingEventsManager
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

    single<UpcomingEventsManager> {
        UpcomingEventsManagerImpl(
            context = androidContext(),
            db = Room.databaseBuilder(
                context = androidContext(),
                UpcomingEventsDb::class.java,
                "upcoming-events-db"
            )
                .fallbackToDestructiveMigration()
                .build()
        )
    }
    single<DNDScheduleCalendarCriteriaManager> {
        DNDScheduleCalendarCriteriaManagerImpl(
            dndScheduleCalendarCriteriaDb = Room.databaseBuilder(
                androidContext(),
                DNDScheduleCalendarCriteriaDb::class.java,
                "dnd-schedule-calendar-db"
            ).fallbackToDestructiveMigration()
                .build()
        )
    }
    single<DNDCalendarScheduler> { DNDCalendarSchedulerImpl(clock = get(),
        context = androidContext(),
        dndScheduleCalendarCriteriaManager = get(),
        upcomingEventsManager = get<UpcomingEventsManager>()) }
}