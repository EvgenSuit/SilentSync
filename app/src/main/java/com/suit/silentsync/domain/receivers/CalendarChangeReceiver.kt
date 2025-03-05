package com.suit.silentsync.domain.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.suit.silentsync.domain.DNDScheduler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CalendarChangeReceiver: BroadcastReceiver(), KoinComponent {
    private val dndScheduler: DNDScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        try {
            dndScheduler.schedule(context)
        } catch (e: Exception) {
            println(e)
            Log.d("EventScheduler", "Exception: $e")
        }
    }
}