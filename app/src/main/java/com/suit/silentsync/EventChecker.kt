package com.suit.silentsync

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventChecker(
    private val contentResolver: ContentResolver
) {
    suspend fun doesEventExist(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            getCursor(id)?.use { it.count != 0 } ?: false
        } catch (e: Exception) {
            Log.d("CalendarReceiver", "Event checker exception: $e")
            false
        }
    }

    // accounts for a case where the user changes start time of an already ongoing event to the future (do nothing)
    suspend fun doTurnDNDon(id: Long, currTime: Long): Boolean = withContext(Dispatchers.IO) {
        getCursor(id)?.use {
            // returns true if event's start time is less than or equal to the curr time (there's almost always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
                it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)) - currTime <= 0
            } else false
        } ?: false
    }

    // accounts for a case where the user changes end time of an already ongoing event to the past (do nothing)
    suspend fun doTurnDNDoff(id: Long, currTime: Long): Boolean = withContext(Dispatchers.IO) {
        getCursor(id)?.use {
            // returns true if event's end time is bigger than or equal to the curr time (there's almost always a small delay in dnd toggle execution)
            if (it.moveToFirst()) {
                it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)) - currTime >= 0
            } else false
        } ?: false
    }

    private fun getCursor(id: Long): Cursor? =
        contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events._ID, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND),
            "${CalendarContract.Events._ID} = ?",
            arrayOf(id.toString()),
            null
        )
}