package com.suit.silentsync.koin

import com.suit.silentsync.domain.EventChecker
import com.suit.silentsync.domain.DNDScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.time.Clock

val dndModule = module {
    single { Clock.systemDefaultZone() }
    factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
    single { EventChecker(
        contentResolver = androidContext().contentResolver,
        clock = get()) }
    single { DNDScheduler(clock = get()) }
}