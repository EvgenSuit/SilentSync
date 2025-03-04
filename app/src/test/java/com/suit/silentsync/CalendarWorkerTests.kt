package com.suit.silentsync

import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.provider.CalendarContract
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import com.suit.silentsync.koin.KoinWorkerFactory
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class CalendarWorkerTests {
    private lateinit var context: Context
    private lateinit var testClock: TestClock
    private lateinit var koin: Koin
    private lateinit var koinWorkerFactory: KoinWorkerFactory

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testClock = TestClock()

        koin = startKoin {
            modules(
                module {
                    // explicitly register clock, otherwise koin wouldn't detect it
                    single<Clock> { testClock }
                }
            )
        }.koin
        koinWorkerFactory = KoinWorkerFactory(koin)
        val workManagerConfig = Configuration.Builder()
            .setWorkerFactory(koinWorkerFactory)
            .build()
        WorkManager.initialize(context, workManagerConfig)
    }

    @Test
    fun eventOccurs_workerScheduled() = runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = startTime + TimeUnit.HOURS.toMillis(1)

        val providerInfo = ProviderInfo().apply {
            authority = CalendarContract.AUTHORITY
            grantUriPermissions = true
        }
        Robolectric.buildContentProvider(SilentSyncCalendarProvider::class.java).create(providerInfo)

        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, ContentValues().apply {
            put(CalendarContract.Events._ID, 1L)
            put(CalendarContract.Events.TITLE, "custom event")
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
        })
        context.contentResolver.update(CalendarContract.Events.CONTENT_URI, ContentValues().apply {
            put(CalendarContract.Events._ID, 1L)
            put(CalendarContract.Events.TITLE, "custom event")
            put(CalendarContract.Events.DTSTART, startTime)
            put(CalendarContract.Events.DTEND, endTime)
        }, null, null)

        val calendarWorker = TestListenableWorkerBuilder<CalendarWorker>(
            context = context
        ).setWorkerFactory(koinWorkerFactory)
            .build()

        val result = calendarWorker.doWork()
        assertEquals(ListenableWorker.Result.success(), result)
    }
}