package com.suit.silentsync

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.suit.feature.dndcalendar.presentation.koin.dndCalendarFeatureModule
import com.suit.feature.dndlocation.koin.dndLocationFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SilentSyncApplication: Application(), LifecycleObserver {

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        startKoin {
            androidContext(this@SilentSyncApplication)
            modules(dndCalendarFeatureModule, dndLocationFeatureModule)
        }
    }
}