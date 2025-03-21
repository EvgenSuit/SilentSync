package com.suit.feature.dndcalendar.presentation.koin

import com.suit.dndCalendar.impl.koin.dndCalendarImplKoinModule
import com.suit.feature.dndcalendar.presentation.DNDCalendarViewModel
import com.suit.utility.koin.utilKoinModule
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

private val dndCalendarModule = module {
    single {
        Dispatchers.IO
    }
    singleOf(::DNDCalendarViewModel)
}

val dndCalendarFeatureModule = module {
    includes(
        utilKoinModule,
        dndCalendarImplKoinModule,
        dndCalendarModule
    )
}