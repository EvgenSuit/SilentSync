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
import java.time.Clock
import java.util.concurrent.TimeUnit

class CalendarWorker(
    appContext: Context,
    params: WorkerParameters,
    private val clock: Clock
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )
            val currTime = clock.millis()
            val cursor: Cursor? = applicationContext.contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                "(${CalendarContract.Events.DTSTART} > ? OR ${CalendarContract.Events.DTEND} > ?) AND ${CalendarContract.Events.TITLE} LIKE ?",
                arrayOf(currTime.toString(), currTime.toString() ,"%custom%"),
                "${CalendarContract.Events.DTSTART} ASC"
            )

            cursor?.use {
                println("Count: ${it.count}")
                if (it.count != 0) {
                    while (it.moveToNext()) {
                        val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                        val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                        val startTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                        val endTime = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                        /*val event = CalendarEventData(eventId, title, startTime, endTime)
                        schedule(event)
                        println("Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime")
                        Log.d(
                            "CALENDAR_WORK",
                            "Event ID: $eventId, Title: $title, Start: $startTime, End: $endTime"
                        )*/
                    }
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
            /*WorkManager.getInstance(applicationContext).enqueueUniqueWork(
                "CalendarWorker",
                ExistingWorkPolicy.REPLACE,
                work
            )*/
            Result.success()
        } catch (e: Exception) {
            println(e)
            Log.e("CALENDAR_WORK", "Error querying calendar events: ${e.message}", e)
            Result.failure()
        }
    }
    private fun schedule(event: CalendarEventData) {
        val currTime = clock.millis()

        // TODO: calculate delay based on current time rounded down to a minute
        val onDelay = event.startTime - currTime
        val onRequest = OneTimeWorkRequestBuilder<DNDOnWorker>()
            // handle a case when a user changes end time of an event when it has already started
            // or when a user views current event, which would emit the CONTENT_URI and execute this worker with a delay of 0
            .setInitialDelay(onDelay.coerceAtLeast(0), TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to event.id))
            .build()
        val offDelay = event.endTime - currTime
        val offRequest = OneTimeWorkRequestBuilder<DNDOffWorker>()
            // even when CONTENT_URI is emitted as a result of a user viewing current event, offDelay still gets calculated correctly
            .setInitialDelay(offDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to event.id))
            .build()

        val instance = WorkManager.getInstance(applicationContext)
        // ensure "dnd off" work executes only if "dnd on" work succeeds
        /*instance.beginUniqueWork(
            "dnd_chain_${event.id}",
            ExistingWorkPolicy.REPLACE,
            onRequest
        )
            .then(offRequest)
            .enqueue()*/
        /*instance.enqueueUniqueWork(
            "${event.id} DND on",
            ExistingWorkPolicy.REPLACE,
            onRequest
        )
        instance.enqueueUniqueWork(
            "${event.id} DND off",
            ExistingWorkPolicy.REPLACE,
            offRequest
        )*/
        Log.d("CALENDAR_WORK", "registered work ${event.title} for ${SimpleDateFormat("yyyy-dd-MM, HH:mm:ss").format(event.startTime)}." +
                "Ending at ${SimpleDateFormat("yyyy-dd-MM, HH:mm:ss").format(event.endTime)} ." +
                "Start delay in hours: ${onDelay / (1000 * 60 * 60)}, minutes: ${onDelay / (1000 * 60)%60}." +
                "Ene delay in hours: ${offDelay / (1000 * 60 * 60)}, minutes: ${offDelay / (1000 * 60)%60}.")
    }
}

data class CalendarEventData(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val deleted: Boolean
)