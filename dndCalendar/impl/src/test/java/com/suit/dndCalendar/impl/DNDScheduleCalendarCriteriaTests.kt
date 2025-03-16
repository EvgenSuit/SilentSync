package com.suit.dndCalendar.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.suit.dndCalendar.impl.data.DNDScheduleCalendarCriteriaManagerImpl
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaDb
import com.suit.dndCalendar.impl.data.criteriaDb.DNDScheduleCalendarCriteriaEntity
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import junit.framework.TestCase.assertEquals
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

    private lateinit var dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManagerImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        criteriaDb = Room.inMemoryDatabaseBuilder(
            context,
            DNDScheduleCalendarCriteriaDb::class.java
        ).allowMainThreadQueries().build()
        dndScheduleCalendarCriteriaManager = DNDScheduleCalendarCriteriaManagerImpl(
            dndScheduleCalendarCriteriaDb = criteriaDb
        )
    }

    @Test
    fun changeCriteria_criteriaReplaced() = runTest {
        val criteriaEntity1 = DNDScheduleCalendarCriteriaEntity(likeNames = listOf("some event"))
        criteriaDb.dao().insertCriteria(criteriaEntity1)
        assertEquals(criteriaEntity1, criteriaDb.dao().criteriaFlow().first())

        val criteriaEntity2 = DNDScheduleCalendarCriteriaEntity(likeNames = listOf("some event 2"))
        dndScheduleCalendarCriteriaManager.changeCriteria(criteriaEntity2.toCriteria())

        assertEquals(criteriaEntity2, criteriaDb.dao().criteriaFlow().first())
    }

    private fun DNDScheduleCalendarCriteriaEntity.toCriteria() =
        DNDScheduleCalendarCriteria(
            likeNames = likeNames,
            attendees = participants
        )
}