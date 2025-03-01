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
        Log.d("CALENDAR_WORK", "Does event exist: ${eventChecker.doesEventExist(id)}")
        if (!eventChecker.doesEventExist(id)) return Result.success()

        applicationContext.getSystemService(NotificationManager::class.java)
            .setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
        return Result.success()
    }
}