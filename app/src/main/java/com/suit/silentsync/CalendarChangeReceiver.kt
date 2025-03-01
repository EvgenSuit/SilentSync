package com.suit.silentsync

import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class CalendarChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val uri: Uri? = intent.data
        if (uri != null) {
            val eventId = ContentUris.parseId(uri)
            Log.d("CalendarReceiver", "Event changed/created: $eventId")

            scheduleWork(context, eventId)
        }
    }
    private fun scheduleWork(context: Context, eventId: Long) {
        val work = OneTimeWorkRequestBuilder<CalendarWorker>()
            .setInputData(androidx.work.Data.Builder().putLong("eventId", eventId).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "CalendarWorker",
            androidx.work.ExistingWorkPolicy.REPLACE,
            work
        )
    }
}