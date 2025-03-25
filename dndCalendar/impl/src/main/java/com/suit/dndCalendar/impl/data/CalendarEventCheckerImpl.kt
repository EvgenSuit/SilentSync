package com.suit.dndCalendar.impl.data

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import com.suit.dndcalendar.api.CalendarEventChecker
import java.time.Clock

internal class CalendarEventCheckerImpl(
    private val contentResolver: ContentResolver,
    private val clock: Clock
): CalendarEventChecker {
    override fun doesEventExist(id: Long) = getCursor(id)?.use { it.count != 0 } ?: false

    override fun doTurnDNDon(id: Long) =
        getCursor(id)?.use {
            // returns true if curr time is bigger than or equal to event's start time (there's always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
                clock.millis() - it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)) >= 0
            } else false
        } ?: false

    override fun doTurnDNDoff(id: Long) =
        getCursor(id)?.use {
            // returns true if event's end time (with tolerance) is bigger than or equal to the curr time (there's always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
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