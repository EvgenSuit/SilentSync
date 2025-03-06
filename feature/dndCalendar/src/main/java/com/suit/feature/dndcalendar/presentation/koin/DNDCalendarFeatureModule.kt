package com.suit.feature.dndcalendar.presentation.koin

import com.suit.dndCalendar.impl.koin.dndCalendarImplKoinModule
import com.suit.feature.dndcalendar.presentation.DNDCalendarViewModel
import org.koin.dsl.module

private val dndCalendarModule = module {
    single { DNDCalendarViewModel(
        dndCalendarScheduler = get()
    ) }
}

val dndCalendarFeatureModule = module {
    includes(
        dndCalendarImplKoinModule,
        dndCalendarModule
    )
}