package com.suit.silentsync

import android.app.Application
import com.suit.feature.dndcalendar.presentation.koin.dndCalendarFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SilentSyncApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SilentSyncApplication)
            modules(dndCalendarFeatureModule)
        }
    }
}