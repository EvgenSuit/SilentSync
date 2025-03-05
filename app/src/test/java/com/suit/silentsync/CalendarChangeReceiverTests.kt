package com.suit.silentsync

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.suit.silentsync.data.CalendarEventData
import com.suit.silentsync.domain.EventChecker
import com.suit.silentsync.domain.DNDScheduler
import com.suit.silentsync.domain.receivers.CalendarChangeReceiver
import com.suit.silentsync.helpers.SilentSyncCalendarProvider
import com.suit.silentsync.helpers.TestHelpers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ContentProviderController
import java.time.Clock
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(RobolectricTestRunner::class)
class CalendarChangeReceiverTests {
    private lateinit var context: Context
    private lateinit var testClock: TestClock
    private lateinit var contentProviderController: ContentProviderController<SilentSyncCalendarProvider>
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testClock = TestClock()
        testScope = TestScope()

        contentProviderController = TestHelpers.buildProvider()

        startKoin {
            modules(module {
                single<Clock> { testClock }
                singleOf(::DNDScheduler)
                single<CoroutineScope> { testScope }
                single { EventChecker(
                    contentResolver = context.contentResolver,
                    clock = testClock) }
            })
        }
    }
    @After
    fun cleanup() {
        contentProviderController.shutdown()
    }

    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_alarmsScheduled() = testScope.runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        val triggerAtMs = scheduledAlarms.map { it.triggerAtMs }
        assertEquals(startTime, triggerAtMs[0])
        assertEquals(endTime, triggerAtMs[1])
    }
    @Test
    fun eventOccurs_endTimeLowerThanCurrTime_alarmsNotScheduled() = testScope.runTest {
        val startTime = testClock.millis() - TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() - TimeUnit.MINUTES.toMillis(5)

        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        assertTrue(shadowAlarmManager.scheduledAlarms.isEmpty())
    }

    private companion object {
        const val CALENDAR_URI = "content://com.android.calendar"
    }
}