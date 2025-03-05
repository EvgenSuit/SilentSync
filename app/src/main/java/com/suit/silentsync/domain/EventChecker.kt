package com.suit.silentsync.domain

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import java.time.Clock

class EventChecker(
    private val contentResolver: ContentResolver,
    private val clock: Clock
) {
    fun doesEventExist(id: Long) = getCursor(id)?.use { it.count != 0 } ?: false

    // below checks are needed in case event changes aren't detected in time by the receiver

    // accounts for a case where the user changes start time of an already active event to the future (do nothing)
    fun doTurnDNDon(id: Long) =
        getCursor(id)?.use {
            // returns true if curr time is bigger than or equal to event's start time (there's always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
                Log.d("EventScheduler", "DTSTART: ${it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))}, curr: ${clock.millis()}")
                clock.millis() - it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)) >= 0
            } else false
        } ?: false

    // accounts for a case where the user changes end time of an already active event closer to the curr time - do nothing
    fun doTurnDNDoff(id: Long) =
        getCursor(id)?.use {
            // returns true if event's end time (with tolerance) is bigger than or equal to the curr time (there's always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
                Log.d("EventScheduler", "DTEND: ${it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))}, curr: ${clock.millis()}")
                it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)) + 5000L - clock.millis() >= 0
            } else false
        } ?: false

    private fun getCursor(id: Long): Cursor? =
        contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND),
            "${CalendarContract.Events._ID} = ?",
            arrayOf(id.toString()),
            null
        )
}