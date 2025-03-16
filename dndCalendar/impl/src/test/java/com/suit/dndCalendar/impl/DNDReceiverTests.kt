package com.suit.dndCalendar.impl

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.CalendarEventData
import com.suit.dndCalendar.impl.data.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.data.DNDActionType
import com.suit.dndCalendar.impl.data.UpcomingEventsManagerImpl
import com.suit.dndCalendar.impl.data.upcomingEventsDb.UpcomingEventsDb
import com.suit.dndCalendar.impl.receivers.DNDReceiver
import com.suit.dndCalendar.impl.helpers.EventCalendarProvider
import com.suit.dndCalendar.impl.helpers.TestHelpers
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.dndcalendar.api.UpcomingEventsManager
import com.suit.utility.test.MainDispatcherRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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
import org.robolectric.android.controller.ContentProviderController
import java.time.Clock


@RunWith(RobolectricTestRunner::class)
class DNDReceiverTests {
    private lateinit var context: Context
    private lateinit var clock: TestClock
    private lateinit var contentProviderController: ContentProviderController<EventCalendarProvider>
    private lateinit var upcomingEventsDb: UpcomingEventsDb

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clock = TestClock()

        contentProviderController = TestHelpers.buildProvider()
        upcomingEventsDb = Room.inMemoryDatabaseBuilder(
            context = context,
            UpcomingEventsDb::class.java
        ).allowMainThreadQueries().build()
        startKoin {
            modules(module {
                single<Clock> { clock }
                single<CalendarEventChecker> { CalendarEventCheckerImpl(
                    contentResolver = context.contentResolver,
                    clock = clock
                ) }
                single<UpcomingEventsManager> {
                    UpcomingEventsManagerImpl(
                        context = context,
                        db = upcomingEventsDb
                    )
                }
                single<CoroutineScope> { TestScope(mainDispatcherRule.dispatcher) }
            })
        }
    }
    @After
    fun teardown() {
        contentProviderController.shutdown()
        upcomingEventsDb.close()
    }

    @Test
    fun eventDoesNotExist_dndNotToggled() = runTest {
        val intent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_ON.name)
        }
        context.sendBroadcast(intent)
        advanceUntilIdle()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        assertEquals(NotificationManager.INTERRUPTION_FILTER_ALL,
            notificationManager.currentInterruptionFilter)
    }

    @Test
    fun eventExists_dndToggled() = runTest {
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "some title", clock.millis(), clock.millis()+1))

        val receiver = DNDReceiver()
        val intent = Intent().apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", 1L)
        }
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
    }

    @Test
    fun eventExists_upcomingEventExists_dndTurnedOff() = runTest {
        upcomingEventsDb.dao().insert(UpcomingEventData(id = 1L, title = "", startTime = 0, endTime = 0))
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "some title", clock.millis(), clock.millis()+1))

        val receiver = DNDReceiver()
        val turnOffIntent = Intent().apply {
            putExtra("action", DNDActionType.DND_OFF.name)
            putExtra("eventId", 1L)
        }
        val turnOnIntent = Intent().apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", 1L)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        receiver.onReceive(context, turnOnIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
        receiver.onReceive(context, turnOffIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_ALL,
            notificationManager.currentInterruptionFilter)
    }
    @Test
    fun eventExists_upcomingEventDoesNotExist_dndNotTurnedOff() = runTest {
        assertTrue(upcomingEventsDb.dao().getUpcomingEvents().first().isEmpty())
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "some title", clock.millis(), clock.millis()+1))

        val receiver = DNDReceiver()
        val turnOffIntent = Intent().apply {
            putExtra("action", DNDActionType.DND_OFF.name)
            putExtra("eventId", 1L)
        }
        val turnOnIntent = Intent().apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", 1L)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        receiver.onReceive(context, turnOnIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
        receiver.onReceive(context, turnOffIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
    }
}