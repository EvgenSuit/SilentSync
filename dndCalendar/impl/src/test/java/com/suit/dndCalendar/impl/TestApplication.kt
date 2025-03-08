package com.suit.dndCalendar.impl

import android.app.Application
import org.koin.core.context.stopKoin

class TestApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        stopKoin()
    }
}