package com.suit.silentsync.dndWorkers

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.suit.silentsync.EventChecker

class DNDOnWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
    private val eventChecker: EventChecker
): CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong("id", 0L)
        Log.d("CALENDAR_WORK", "DND ON Worker: Does event exist: ${eventChecker.doesEventExist(id)}")
        if (!eventChecker.doesEventExist(id)) return Result.success()

        val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)

        Log.d("CALENDAR_WORK", "DND ON Worker: Turning DND on: ${notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_PRIORITY}")
        // avoid turning DND on if a user views current event, which would emit the CONTENT_URI and execute this worker with a delay of 0
        if (notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
        return Result.success()
    }
}