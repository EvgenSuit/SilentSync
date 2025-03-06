package com.suit.dndCalendar.impl

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.CalendarEventData
import com.suit.dndCalendar.impl.domain.CalendarEventCheckerImpl
import com.suit.dndCalendar.impl.domain.DNDActionType
import com.suit.dndCalendar.impl.domain.receivers.DNDReceiver
import com.suit.dndCalendar.impl.helpers.SilentSyncCalendarProvider
import com.suit.dndCalendar.impl.helpers.TestHelpers
import com.suit.dndcalendar.api.CalendarEventChecker
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ContentProviderController
import org.robolectric.annotation.LooperMode
import java.time.Clock

@LooperMode(LooperMode.Mode.LEGACY)
@RunWith(RobolectricTestRunner::class)
class DNDReceiverTests {
    private lateinit var context: Context
    private lateinit var clock: TestClock
    private lateinit var testScope: TestScope
    private lateinit var contentProviderController: ContentProviderController<SilentSyncCalendarProvider>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clock = TestClock()
        testScope = TestScope()

        contentProviderController = TestHelpers.buildProvider()

        startKoin {
            modules(module {
                single<Clock> { clock }
                single<CalendarEventChecker> { CalendarEventCheckerImpl(
                    contentResolver = context.contentResolver,
                    clock = clock
                ) }
                single<CoroutineScope> { testScope }
            })
        }
    }
    @After
    fun teardown() {
        contentProviderController.shutdown()
    }

    @Test
    fun eventDoesNotExist_dndNotToggled() = testScope.runTest {
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
    fun eventExists_dndToggled() = testScope.runTest {
        TestHelpers.insert(context, CalendarEventData(1L, "some title", clock.millis(), clock.millis()+1))
        val intent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", 1L)
        }
        context.sendBroadcast(intent)
        advanceUntilIdle()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
    }

    @Test
    fun eventExists_dndTurnedOff() = testScope.runTest {
        TestHelpers.insert(context, CalendarEventData(1L, "some title", clock.millis(), clock.millis()+1))
        val turnOffIntent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_OFF.name)
            putExtra("eventId", 1L)
        }
        val turnOnIntent = Intent(context, DNDReceiver::class.java).apply {
            putExtra("action", DNDActionType.DND_ON.name)
            putExtra("eventId", 1L)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        context.sendBroadcast(turnOnIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY,
            notificationManager.currentInterruptionFilter)
        context.sendBroadcast(turnOffIntent)
        advanceUntilIdle()
        assertEquals(NotificationManager.INTERRUPTION_FILTER_ALL,
            notificationManager.currentInterruptionFilter)
    }
}