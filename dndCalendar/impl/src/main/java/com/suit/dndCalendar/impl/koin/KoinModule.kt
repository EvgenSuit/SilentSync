package com.suit.dndCalendar.impl.koin

import com.suit.dndCalendar.impl.domain.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.domain.DNDCalendarSchedulerImpl
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.DNDCalendarScheduler
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
    single<DNDCalendarScheduler> { DNDCalendarSchedulerImpl(clock = get(),
        context = androidContext()) }
}