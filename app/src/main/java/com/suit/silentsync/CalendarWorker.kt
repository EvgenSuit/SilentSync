package com.suit.silentsync

import android.content.Context
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.suit.silentsync.dndWorkers.DNDOffWorker
import com.suit.silentsync.dndWorkers.DNDOnWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.concurrent.TimeUnit

class CalendarWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )
            val currTime = Instant.now().toEpochMilli()
            val cursor: Cursor? = applicationContext.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                "(${CalendarContract.Events.DTSTART} > ? OR ${CalendarContract.Events.DTEND} > ?) AND ${CalendarContract.Events.TITLE} LIKE ?",
                arrayOf(currTime.toString(), currTime.toString() ,"%custom%"),
                "${CalendarContract.Events.DTSTART} ASC"
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    do {
                        val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                        val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                        val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                        val event = CustomCalendarEvent(eventId, title, startTime, endTime)
                        schedule(event)
                        Log.d(
                            "CALENDAR_WORK",
                            "Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime"
                        )
                    } while (it.moveToNext())
                } else {
                    Log.d("CALENDAR_WORK", "No calendar events found.")
                }
            }
            val work = OneTimeWorkRequestBuilder<CalendarWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .addContentUriTrigger(CalendarContract.Events.CONTENT_URI, true)
                        .build())
                .build()
            WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "CalendarWorker",
                ExistingWorkPolicy.REPLACE,
                work
            )
            Result.success()
        } catch (e: Exception) {
            Log.e("CALENDAR_WORK", "Error querying calendar events: ${e.message}", e)
            Result.failure()
        }
    }
    private fun schedule(event: CustomCalendarEvent) {
        val currTime = Instant.now().toEpochMilli()

        val onDelay = event.startTime - currTime
        val onRequest = OneTimeWorkRequestBuilder<DNDOnWorker>()
            // handle a case when a user changes end time of an event when it has already started
            .setInitialDelay(onDelay.let { if (it < 0) 0L else it }, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to event.id))
            .build()
        val instance = WorkManager.getInstance(applicationContext)
        instance.enqueueUniqueWork(
            "${event.id} DND on",
            ExistingWorkPolicy.REPLACE,
            onRequest
        )

        val offDelay = event.endTime - currTime
        val offRequest = OneTimeWorkRequestBuilder<DNDOffWorker>()
            .setInitialDelay(offDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to event.id))
            .build()
        instance.enqueueUniqueWork(
            "${event.id} DND off",
            ExistingWorkPolicy.REPLACE,
            offRequest
        )
        Log.d("CALENDAR_WORK", "registered work ${event.title} for ${SimpleDateFormat("yyyy-dd-MM, HH:mm:ss").format(event.startTime)}." +
                "Ending at ${SimpleDateFormat("yyyy-dd-MM, HH:mm:ss").format(event.endTime)} ." +
                "Start delay in hours: ${onDelay / (1000 * 60 * 60)}, minutes: ${onDelay / (1000 * 60)%60}")
    }
}

data class CustomCalendarEvent(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long
)