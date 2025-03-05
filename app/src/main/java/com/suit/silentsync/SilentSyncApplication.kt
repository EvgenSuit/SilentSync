package com.suit.silentsync

import android.app.Application
import com.suit.silentsync.koin.dndModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SilentSyncApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SilentSyncApplication)
            modules(dndModule)
        }
    }
}