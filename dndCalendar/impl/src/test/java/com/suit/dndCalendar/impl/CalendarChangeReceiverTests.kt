package com.suit.dndCalendar.impl

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.data.CalendarEventData
import com.suit.dndCalendar.impl.data.DNDCalendarSchedulerImpl
import com.suit.dndCalendar.impl.data.DNDScheduleCalendarCriteriaManagerImpl
import com.suit.dndCalendar.impl.data.db.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.db.DNDScheduleCalendarCriteriaEntity
import com.suit.dndCalendar.impl.helpers.SilentSyncCalendarProvider
import com.suit.dndCalendar.impl.helpers.TestHelpers
import com.suit.dndCalendar.impl.receivers.CalendarChangeReceiver
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.utility.test.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ContentProviderController
import java.time.Clock
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CalendarChangeReceiverTests {
    private lateinit var context: Context
    private lateinit var testClock: TestClock
    private lateinit var contentProviderController: ContentProviderController<SilentSyncCalendarProvider>
    private lateinit var db: DNDScheduleCalendarCriteriaDb
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testClock = TestClock()

        contentProviderController = TestHelpers.buildProvider()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DNDScheduleCalendarCriteriaDb::class.java
        ).allowMainThreadQueries().build()
        startKoin {
            modules(module {
                single<Clock> { testClock }
                single<CoroutineScope> { TestScope() }
                single<DNDCalendarScheduler> { DNDCalendarSchedulerImpl(
                    context = context,
                    clock = get(),
                    dndScheduleCalendarCriteriaManager = DNDScheduleCalendarCriteriaManagerImpl(db = db)
                ) }
                single<CalendarEventChecker> { CalendarEventCheckerImpl(
                    contentResolver = context.contentResolver,
                    clock = testClock) }
            })
        }

    }
    @After
    fun cleanup() {
        contentProviderController.shutdown()
        db.close()
    }

    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaPresent_criteriaMatches_alarmsScheduled() = runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

        insertEntity(DNDScheduleCalendarCriteriaEntity(likeName = "event"))
        TestHelpers.insert(context, CalendarEventData(1L, "like name event", startTime, endTime))

        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        val triggerAtMs = scheduledAlarms.map { it.triggerAtMs }
        assertEquals(startTime, triggerAtMs[0])
        assertEquals(endTime, triggerAtMs[1])
    }
    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaPresent_criteriaDoesNotMatch_alarmsNotScheduled() = runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

        insertEntity(DNDScheduleCalendarCriteriaEntity(likeName = "some"))
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertTrue(scheduledAlarms.isEmpty())
    }
    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaNotPresent_alarmsNotScheduled() = runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

        TestHelpers.insert(context, CalendarEventData(1L, "like name event", startTime, endTime))

        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertTrue(scheduledAlarms.isEmpty())
    }

    @Test
    fun eventOccurs_endTimeLowerThanCurrTime_alarmsNotScheduled() = runTest {
        val startTime = testClock.millis() - TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() - TimeUnit.MINUTES.toMillis(5)

        insertEntity(DNDScheduleCalendarCriteriaEntity(likeName = "event"))
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

    private suspend fun insertEntity(entity: DNDScheduleCalendarCriteriaEntity
    =DNDScheduleCalendarCriteriaEntity(likeName = "like name event")) {
        db.dao().apply {
            replaceCriteria(entity)
            assertEquals(entity, criteriaFlow().first())
        }
    }

    private companion object {
        const val CALENDAR_URI = "content://com.android.calendar"
    }
}