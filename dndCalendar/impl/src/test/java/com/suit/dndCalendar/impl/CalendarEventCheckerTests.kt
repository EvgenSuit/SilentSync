package com.suit.dndCalendar.impl

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.CalendarEventData
import com.suit.dndCalendar.impl.data.CalendarEventCheckerImpl
import com.suit.dndcalendar.api.CalendarEventChecker
import com.suit.dndCalendar.impl.helpers.EventCalendarProvider
import com.suit.dndCalendar.impl.helpers.TestHelpers
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ContentProviderController
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class CalendarEventCheckerTests {
    private lateinit var calendarEventChecker: CalendarEventChecker
    private lateinit var context: Context
    private lateinit var clock: TestClock
    private lateinit var contentProviderController: ContentProviderController<EventCalendarProvider>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clock = TestClock()

        contentProviderController = TestHelpers.buildProvider()

        calendarEventChecker = CalendarEventCheckerImpl(
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
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertTrue(calendarEventChecker.doTurnDNDon(1L))
    }
    @Test
    fun doTurnDNDOn_startBeforeCurrent_true() {
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "custom event", clock.millis()-10, clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertTrue(calendarEventChecker.doTurnDNDon(1L))
    }
    @Test
    fun doTurnDNDOn_false() {
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "custom event", clock.millis()+1, clock.millis() + TimeUnit.MINUTES.toMillis(5)))
        assertFalse(calendarEventChecker.doTurnDNDon(1L))
    }

    @Test
    fun doTurnDNDOff_true() {
        val delay = TimeUnit.MINUTES.toMillis(5)
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() + delay))
        clock.advance(delay)
        assertTrue(calendarEventChecker.doTurnDNDoff(1L))
    }
    @Test
    fun doTurnDNDOff_scheduleToPast_false() {
        TestHelpers.insertCalendarData(context, CalendarEventData(1L, "custom event", clock.millis(), clock.millis() - TimeUnit.MINUTES.toMillis(5)))
        assertFalse(calendarEventChecker.doTurnDNDoff(1L))
    }
}