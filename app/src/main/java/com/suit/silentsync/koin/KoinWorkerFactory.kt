package com.suit.silentsync.koin

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.suit.silentsync.CalendarWorker
import com.suit.silentsync.dndWorkers.DNDOffWorker
import com.suit.silentsync.dndWorkers.DNDOnWorker
import org.koin.core.Koin

class KoinWorkerFactory(
    private val koin: Koin
): WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return when (workerClassName) {
            CalendarWorker::class.java.name -> CalendarWorker(
                appContext,
                workerParameters
            )
            DNDOnWorker::class.java.name -> DNDOnWorker(
                appContext,
                workerParameters,
                koin.get()
            )
            DNDOffWorker::class.java.name -> DNDOffWorker(
                appContext,
                workerParameters,
                koin.get()
            )
            else -> throw RuntimeException("No DND worker provided")
        }
    }
}