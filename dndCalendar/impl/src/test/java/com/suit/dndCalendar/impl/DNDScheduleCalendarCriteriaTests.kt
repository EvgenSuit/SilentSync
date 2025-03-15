package com.suit.dndCalendar.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.DNDScheduleCalendarCriteriaManagerImpl
import com.suit.dndCalendar.impl.data.UpcomingEventsManagerImpl
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaEntity
import com.suit.dndCalendar.impl.data.upcomingEventsDb.UpcomingEventsDb
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.UpcomingEventData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DNDScheduleCalendarCriteriaTests {
    private lateinit var context: Context
    private lateinit var criteriaDb: DNDScheduleCalendarCriteriaDb
    private lateinit var upcomingEventsDb: UpcomingEventsDb

    private lateinit var dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManagerImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        criteriaDb = Room.inMemoryDatabaseBuilder(
            context,
            DNDScheduleCalendarCriteriaDb::class.java
        ).allowMainThreadQueries().build()
        upcomingEventsDb = Room.inMemoryDatabaseBuilder(
            context,
            UpcomingEventsDb::class.java
        ).allowMainThreadQueries().build()
        val upcomingEventsManager = UpcomingEventsManagerImpl(
            context = context, db = upcomingEventsDb
        )
        dndScheduleCalendarCriteriaManager = DNDScheduleCalendarCriteriaManagerImpl(
            dndScheduleCalendarCriteriaDb = criteriaDb,
            upcomingEventsManager = upcomingEventsManager
        )
    }

    @Test
    fun changeCriteria_criteriaReplacedAndUpcomingTogglesDeleted() = runTest {
        val criteriaEntity1 = DNDScheduleCalendarCriteriaEntity(likeName = "some event")
        upcomingEventsDb.dao().insert(UpcomingEventData(1L, title = criteriaEntity1.likeName, startTime = 0, endTime = 0))
        assertFalse(upcomingEventsDb.dao().getUpcomingEvents().first().isEmpty())
        criteriaDb.dao().insertCriteria(criteriaEntity1)
        assertEquals(criteriaEntity1, criteriaDb.dao().criteriaFlow().first())

        val criteriaEntity2 = DNDScheduleCalendarCriteriaEntity(likeName = "some event 2")
        dndScheduleCalendarCriteriaManager.changeCriteria(criteriaEntity2.toCriteria())

        assertTrue(upcomingEventsDb.dao().getUpcomingEvents().first().isEmpty())
        assertEquals(criteriaEntity2, criteriaDb.dao().criteriaFlow().first())
    }

    private fun DNDScheduleCalendarCriteriaEntity.toCriteria() =
        DNDScheduleCalendarCriteria(
            likeName = likeName
        )
}