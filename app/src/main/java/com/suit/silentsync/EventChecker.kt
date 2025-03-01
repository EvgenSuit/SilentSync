package com.suit.silentsync

import android.content.ContentResolver
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventChecker(
    private val contentResolver: ContentResolver
) {
    suspend fun doesEventExist(id: Long): Boolean = withContext(Dispatchers.IO) {
        val projection = arrayOf(CalendarContract.Events._ID)
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events._ID} = ?",
            arrayOf(id.toString()),
            null
        )
        cursor?.use {
            it.moveToFirst()
        } ?: false
    }
}