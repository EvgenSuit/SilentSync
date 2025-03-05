package com.suit.silentsync.domain

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import com.suit.silentsync.data.CalendarEventData
import com.suit.silentsync.domain.receivers.DNDReceiver
import java.text.SimpleDateFormat
import java.time.Clock

class DNDScheduler(
    private val clock: Clock
) {
    fun schedule(context: Context) {
        // TODO: execute below code only if criteria is given
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DELETED
        )
        println("Executing EventScheduler")
        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.TITLE} LIKE ?",
            // TODO: replace criteria with user-defined string (with datastore)
            arrayOf("%custom%"),
            null
        )
        Log.d(
            "EventScheduler",
            "Executing EventScheduler"
        )
        cursor?.use {
            if (it.count != 0) {
                while (it.moveToNext()) {
                    val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                    val title =
                        it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                    val startTime =
                        it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                    val endTime =
                        it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                    val deleted =
                        it.getInt(it.getColumnIndexOrThrow(CalendarContract.Events.DELETED))
                    val event = CalendarEventData(eventId, title, startTime, endTime, deleted == 1)
                    scheduleAlarms(context, event)

                    println("Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime, Is deleted: $deleted")
                    Log.d(
                        "EventScheduler",
                        "Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime, Is deleted: $deleted"
                    )
                }
            } else {
                Log.d("EventScheduler", "No calendar events found.")
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarms(context: Context, event: CalendarEventData) {
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
            context, (event.id*2).toInt(), dndOnIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dndOffPendingIntent = PendingIntent.getBroadcast(
            context, (event.id*2+1).toInt(), dndOffIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        when (event.deleted || event.endTime < clock.millis()) {
            true -> {
                alarmManager.cancel(dndOnPendingIntent)
                alarmManager.cancel(dndOffPendingIntent)
                Log.d(
                    "EventScheduler",
                    "Removed dnd toggle: $event"
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
                println("Scheduled DND ON at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                        "and DND OFF at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}.")
                Log.d(
                    "EventScheduler",
                    "Scheduled DND ON at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.startTime)} " +
                            "and DND OFF at ${SimpleDateFormat("yyyy-MM-dd, HH:mm:ss").format(event.endTime)}."
                )
            }
        }
    }
}

enum class DNDActionType {
    DND_ON, DND_OFF
}