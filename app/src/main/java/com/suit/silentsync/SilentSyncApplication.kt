package com.suit.silentsync

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.suit.silentsync.koin.KoinWorkerFactory
import com.suit.silentsync.koin.dndModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SilentSyncApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@SilentSyncApplication)
            modules(dndModule)
        }

        val config = Configuration.Builder()
            .setWorkerFactory(get<KoinWorkerFactory>())
            .build()
        WorkManager.initialize(this, config)
    }
}