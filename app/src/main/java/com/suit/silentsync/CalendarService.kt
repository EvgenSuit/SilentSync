package com.suit.silentsync

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat

class CalendarService: Service() {
    private val calendarBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("CUSTOM_CALENDAR_EVENT", "${intent?.action}, ${intent?.data}")
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("CUSTOM_CALENDAR_EVENT", "registering receiver")
        registerReceiver(
            calendarBroadcastReceiver,
            IntentFilter("android.intent.action.PROVIDER_CHANGED"),
            RECEIVER_EXPORTED
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CUSTOM_CALENDAR_EVENT", "UNregistering receiver")
        unregisterReceiver(calendarBroadcastReceiver)
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}