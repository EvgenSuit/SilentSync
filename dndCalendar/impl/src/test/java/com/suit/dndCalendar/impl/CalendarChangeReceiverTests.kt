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
import com.suit.dndCalendar.impl.data.UpcomingEventsManagerImpl
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaEntity
import com.suit.dndCalendar.impl.data.upcomingEventsDb.UpcomingEventsDb
import com.suit.dndCalendar.impl.helpers.SilentSyncCalendarProvider
import com.suit.dndCalendar.impl.helpers.TestHelpers
import com.suit.dndCalendar.impl.receivers.CalendarChangeReceiver
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.utility.NoCalendarCriteriaFound
import com.suit.utility.test.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
class CalendarChangeReceiverTests {
    private lateinit var context: Context
    private val testClock: TestClock = TestClock()
    private lateinit var contentProviderController: ContentProviderController<SilentSyncCalendarProvider>
    private lateinit var criteriaDb: DNDScheduleCalendarCriteriaDb
    private lateinit var upcomingEventsDb: UpcomingEventsDb
    private lateinit var dndCalendarScheduler: DNDCalendarSchedulerImpl

    private val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
    private val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        contentProviderController = TestHelpers.buildProvider()
        criteriaDb = Room.inMemoryDatabaseBuilder(
            context,
            DNDScheduleCalendarCriteriaDb::class.java
        ).allowMainThreadQueries().build()
        upcomingEventsDb = Room.inMemoryDatabaseBuilder(
            context,
            UpcomingEventsDb::class.java
        ).allowMainThreadQueries().build()

        val upcomingEventsManager = UpcomingEventsManagerImpl(
            context, db = upcomingEventsDb
        )
        dndCalendarScheduler = DNDCalendarSchedulerImpl(
            context = context,
            clock = testClock,
            upcomingEventsManager = upcomingEventsManager,
            dndScheduleCalendarCriteriaManager = DNDScheduleCalendarCriteriaManagerImpl(
                dndScheduleCalendarCriteriaDb = criteriaDb
            )
        )
        startKoin {
            modules(module {
                single<Clock> { testClock }
                single<CoroutineScope> { TestScope() }
                single<DNDCalendarScheduler> { dndCalendarScheduler }
                single<CalendarEventChecker> { CalendarEventCheckerImpl(
                    contentResolver = context.contentResolver,
                    clock = testClock) }
            })
        }

    }
    @After
    fun cleanup() {
        contentProviderController.shutdown()
        criteriaDb.close()
        upcomingEventsDb.close()
    }

    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaPresent_criteriaMatches_alarmsScheduled() = runTest {
        val startTime = testClock.millis() + TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() + TimeUnit.MINUTES.toMillis(15)

        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        TestHelpers.insert(context, CalendarEventData(1L, "like name event", startTime, endTime))

        triggerEvent()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        val triggerAtMs = scheduledAlarms.map { it.triggerAtMs }
        assertEquals(startTime, triggerAtMs[0])
        assertEquals(endTime, triggerAtMs[1])
    }
    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaPresent_criteriaDoesNotMatch_alarmsNotScheduled() = runTest {
        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("some")))
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        triggerEvent()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertTrue(scheduledAlarms.isEmpty())
    }
    @Test
    fun eventOccurs_endTimeBiggerThanCurrTime_criteriaNotPresent_alarmsNotScheduled() = runTest {
        TestHelpers.insert(context, CalendarEventData(1L, "like name event", startTime, endTime))

        triggerEvent()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        val scheduledAlarms = shadowAlarmManager.scheduledAlarms
        assertTrue(scheduledAlarms.isEmpty())
    }
    @Test
    fun eventOccurs_endTimeLowerThanCurrTime_alarmsNotScheduled() = runTest {
        val startTime = testClock.millis() - TimeUnit.MINUTES.toMillis(10)
        val endTime = testClock.millis() - TimeUnit.MINUTES.toMillis(5)

        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        triggerEvent()

        val shadowAlarmManager = shadowOf(context.getSystemService(AlarmManager::class.java))
        assertTrue(shadowAlarmManager.scheduledAlarms.isEmpty())
    }

    @Test
    fun eventOccurs_eventsNotMatchingCriteriaPresent_eventsRemoved() = runTest {
        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        val event1 = CalendarEventData(1L, "custom event", startTime, endTime)
        val event2 = CalendarEventData(2L, "some", startTime, endTime)
        val event3 = CalendarEventData(3L, "non-matching", startTime, endTime)
        TestHelpers.apply {
            insert(context, event1)
            insert(context, event2)
            insert(context, event3)
        }

        val upcomingEventData = event1.toUpcomingEvent()
        val upcomingEventData2 = event2.toUpcomingEvent()
        val upcomingEventData3 = event3.toUpcomingEvent()
        upcomingEventsDb.dao().apply {
            insert(upcomingEventData)
            insert(upcomingEventData2)
            insert(upcomingEventData3)
        }
        triggerEvent()

        val savedUpcomingEvents = upcomingEventsDb.dao().getUpcomingEvents().first()
        assertTrue(savedUpcomingEvents.contains(upcomingEventData))
        assertFalse(savedUpcomingEvents.contains(upcomingEventData2))
        assertFalse(savedUpcomingEvents.contains(upcomingEventData3))
    }
    @Test
    fun eventOccurs_allUpcomingEventsMatchCriteria_noEventsRemoved() = runTest {
        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        val event1 = CalendarEventData(1L, "custom event", startTime, endTime)
        val event2 = CalendarEventData(2L, "some event", startTime, endTime)
        val event3 = CalendarEventData(3L, "some event 2", startTime, endTime)
        TestHelpers.apply {
            insert(context, event1)
            insert(context, event2)
            insert(context, event3)
        }

        val upcomingEventData = event1.toUpcomingEvent()
        val upcomingEventData2 = event2.toUpcomingEvent()
        val upcomingEventData3 = event3.toUpcomingEvent()
        upcomingEventsDb.dao().apply {
            insert(upcomingEventData)
            insert(upcomingEventData2)
            insert(upcomingEventData3)
        }
        triggerEvent()

        val savedUpcomingEvents = upcomingEventsDb.dao().getUpcomingEvents().first()
        assertTrue(savedUpcomingEvents.contains(upcomingEventData))
        assertTrue(savedUpcomingEvents.contains(upcomingEventData2))
        assertTrue(savedUpcomingEvents.contains(upcomingEventData3))
    }

    @Test
    fun sync_criteriaEmpty_upcomingEventsEmpty_exceptionThrown() = runTest {
        assertFailsWith<NoCalendarCriteriaFound> { dndCalendarScheduler.schedule() }
    }
    @Test
    fun sync_criteriaEmpty_upcomingEventsNotEmpty_upcomingEventsRemovedExceptionNotThrown() = runTest {
        val event1 = CalendarEventData(1L, "custom event", startTime, endTime)
        upcomingEventsDb.dao().insert(event1.toUpcomingEvent())

        dndCalendarScheduler.schedule()

        assertTrue(upcomingEventsDb.dao().getUpcomingEvents().firstOrNull().isNullOrEmpty())
    }

    @Test
    fun eventOccurs_upcomingToggleNotPresent_defaultOptionsUsed() = runTest {
        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        val upcomingEventData = UpcomingEventData(1L, "event", startTime, endTime, scheduleDndOn = false, scheduleDndOff = false)
        triggerEvent()

        val savedUpcomingEvent = upcomingEventsDb.dao().getUpcomingEvents().first().first { it.id == upcomingEventData.id }
        assertTrue(savedUpcomingEvent.scheduleDndOn)
        assertTrue(savedUpcomingEvent.scheduleDndOff)
    }
    @Test
    fun eventOccurs_upcomingTogglePresent_doNotSchedule_savedOptionsUsed() = runTest {
        insertEntity(DNDScheduleCalendarCriteriaEntity(likeNames = listOf("event")))
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", startTime, endTime))

        val upcomingEventData = UpcomingEventData(1L, "event", startTime, endTime, scheduleDndOn = false, scheduleDndOff = false)
        upcomingEventsDb.dao().insert(upcomingEventData)
        triggerEvent()

        val savedUpcomingEvent = upcomingEventsDb.dao().getUpcomingEvents().first().first { it.id == upcomingEventData.id }
        assertEquals(upcomingEventData.scheduleDndOn, savedUpcomingEvent.scheduleDndOn)
        assertEquals(upcomingEventData.scheduleDndOff, savedUpcomingEvent.scheduleDndOff)
    }

    private suspend fun insertEntity(entity: DNDScheduleCalendarCriteriaEntity
    =DNDScheduleCalendarCriteriaEntity(likeNames = listOf("like name event"))) {
        criteriaDb.dao().apply {
            replaceCriteria(entity)
            assertEquals(entity, criteriaFlow().first())
        }
    }

    private fun TestScope.triggerEvent() {
        val receiver = CalendarChangeReceiver()
        // intent doesn't matter here
        val intent = Intent(Intent.ACTION_PROVIDER_CHANGED).apply {
            data = Uri.parse(CALENDAR_URI)
        }
        receiver.onReceive(context, intent)
        advanceUntilIdle()
    }

    private companion object {
        const val CALENDAR_URI = "content://com.android.calendar"
    }
}