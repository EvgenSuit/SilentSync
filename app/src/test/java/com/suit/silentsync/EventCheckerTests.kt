package com.suit.silentsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.suit.silentsync.data.CalendarEventData
import com.suit.silentsync.domain.EventChecker
import com.suit.silentsync.helpers.SilentSyncCalendarProvider
import com.suit.silentsync.helpers.TestHelpers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ContentProviderController
import java.util.concurrent.TimeUnit
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class EventCheckerTests {
    private lateinit var eventChecker: EventChecker
    private lateinit var context: Context
    private lateinit var clock: TestClock
    private lateinit var contentProviderController: ContentProviderController<SilentSyncCalendarProvider>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clock = TestClock()

        contentProviderController = TestHelpers.buildProvider()

        eventChecker = EventChecker(
            contentResolver = context.contentResolver,
            clock = clock
        )
    }
    @After
    fun teardown() {
        contentProviderController.shutdown()
    }

    @Test
    fun doTurnDNDOn_startAtCurrent_true() {
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertTrue(eventChecker.doTurnDNDon(1L))
    }
    @Test
    fun doTurnDNDOn_startBeforeCurrent_true() {
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", clock.millis()-10, clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertTrue(eventChecker.doTurnDNDon(1L))
    }
    @Test
    fun doTurnDNDOn_false() {
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", clock.millis()+1, clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertFalse(eventChecker.doTurnDNDon(1L))
    }

    @Test
    fun doTurnDNDOff_true() {
        val delay = TimeUnit.MINUTES.toMillis(5)
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() + delay))
        clock.advance(delay)
        assertTrue(eventChecker.doTurnDNDoff(1L))
    }
    @Test
    fun doTurnDNDOff_scheduleToPast_false() {
        TestHelpers.insert(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() - TimeUnit.MINUTES.toMillis(5)))
        assertFalse(eventChecker.doTurnDNDoff(1L))
    }
}