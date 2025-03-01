package com.suit.silentsync.koin

import com.suit.silentsync.EventChecker
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dndModule = module {
    single { EventChecker(contentResolver = androidContext().contentResolver) }
    single { KoinWorkerFactory(getKoin()) }
}