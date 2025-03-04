package com.suit.silentsync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat

class CalendarChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.DELETED
            )
            val currTime = System.currentTimeMillis()
            val cursor: Cursor? = context.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                "(${CalendarContract.Events.DTSTART} > ? OR ${CalendarContract.Events.DTEND} > ?) AND ${CalendarContract.Events.TITLE} LIKE ?",
                // TODO: replace criteria with user-defined string (with datastore)
                arrayOf(currTime.toString(), currTime.toString() ,"%custom%"),
                "${CalendarContract.Events.DTSTART} ASC"
            )
            Log.d(
                "CalendarReceiver",
                "Executing CalendarChangeReceiver"
            )
            cursor?.use {
                println("Count: ${it.count}")
                if (it.count != 0) {
                    while (it.moveToNext()) {
                        val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                        val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                        val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                        val deleted = it.getInt(it.getColumnIndexOrThrow(CalendarContract.Events.DELETED))
                        val event = CalendarEventData(eventId, title, startTime, endTime, deleted == 1)
                        scheduleWork(context, event)

                        Log.d(
                            "CalendarReceiver",
                            "Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime, Is deleted: $deleted"
                        )
                    }
                } else {
                    Log.d("CalendarReceiver", "No calendar events found.")
                }
            }
        } catch (e: Exception) {
            Log.d("CalendarReceiver", "Exception: $e")
        }
    }
    private fun scheduleWork(context: Context, event: CalendarEventData) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val dndOnIntent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", event.id)
        }
        val dndOffIntent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_OFF.name)
            putExtra("eventId", event.id)
        }

        val dndOnPendingIntent = PendingIntent.getBroadcast(
            context, (event.id).toInt(), dndOnIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dndOffPendingIntent = PendingIntent.getBroadcast(
            context, (event.id+1).toInt(), dndOffIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms())
            || alarmManager.canScheduleExactAlarms()) {
            when (event.deleted) {
                true -> {
                    alarmManager.cancel(dndOnPendingIntent)
                    alarmManager.cancel(dndOffPendingIntent)
                    Log.d(
                        "CalendarReceiver",
                        "Removed dnd toggle"
                    )
                }
                false -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        event.startTime,
                        dndOnPendingIntent
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        event.endTime,
                        dndOffPendingIntent
                    )
                    Log.d(
                        "CalendarReceiver",
                        "Scheduled DND ON at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                                "and DND OFF at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}."
                    )
                }
            }

        }
    }
}

enum class DNDActionType {
    DND_ON, DND_OFF
}