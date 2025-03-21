package com.suit.feature.dndcalendar

import app.cash.turbine.test
import com.suit.dndcalendar.api.DNDCalendarScheduler
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteriaManager
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.dndcalendar.api.UpcomingEventsManager
import com.suit.feature.dndcalendar.presentation.DNDCalendarCriteriaDeletion
import com.suit.feature.dndcalendar.presentation.DNDCalendarIntent
import com.suit.feature.dndcalendar.presentation.DNDCalendarViewModel
import com.suit.testutil.test.MainDispatcherRule
import com.suit.testutil.test.analyticsMock
import com.suit.utility.NoCalendarCriteriaFound
import com.suit.utility.analytics.SilentSyncAnalytics
import com.suit.utility.ui.CustomResult
import com.suit.utility.ui.DNDCalendarUIEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DNDCalendarViewModelTests {
    private lateinit var dndCalendarScheduler: DNDCalendarScheduler
    private lateinit var dndScheduleCalendarCriteriaManager: DNDScheduleCalendarCriteriaManager
    private lateinit var viewModel: DNDCalendarViewModel
    private lateinit var upcomingEventsManager: UpcomingEventsManager
    private lateinit var analyticsMock: SilentSyncAnalytics

    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val criteria = DNDScheduleCalendarCriteria(likeNames = listOf("event"),
        attendees = listOf("evgen"))
    private val upcomingEventData = UpcomingEventData(1L, "Some event", 0, 0)

    private fun setup(
        isCriteriaPresent: Boolean = true,
        isUpcomingEventPresent: Boolean = true,
        scheduleException: Exception? = null,
        toggleDNDModeException: Exception? = null
    ) {
        dndCalendarScheduler = mockk {
            coEvery { schedule() } answers {
                if (!isCriteriaPresent && scheduleException == null) throw NoCalendarCriteriaFound("")
                else if (scheduleException != null) throw scheduleException
            }
        }
        dndScheduleCalendarCriteriaManager = mockk {
            every { getCriteria() } returns flowOf(if (isCriteriaPresent) criteria else null)
            coEvery { changeCriteria(any()) } returns Unit
        }
        upcomingEventsManager = mockk(relaxed = true) {
            coEvery { setDndOffToggle(id = any(), endTime = any()) } answers {
                if (toggleDNDModeException != null) throw toggleDNDModeException
            }
            coEvery { setDndOnToggle(id = any(), startTime = any()) } answers {
                if (toggleDNDModeException != null) throw toggleDNDModeException
            }
            every { upcomingEventsFlow() } returns flowOf(if (isUpcomingEventPresent) listOf(upcomingEventData) else listOf())
        }
        analyticsMock = analyticsMock()
        viewModel = DNDCalendarViewModel(
            dndCalendarScheduler = dndCalendarScheduler,
            dndScheduleCalendarCriteriaManager = dndScheduleCalendarCriteriaManager,
            upcomingEventsManager = upcomingEventsManager,
            dispatcher = mainDispatcherRule.dispatcher,
            silentSyncAnalytics = analyticsMock
        )
    }

    @Test
    fun onStart_fetchCriteriaAndSchedule_criteriaNotPresent_fetchResultError() = runTest {
        setup(isCriteriaPresent = false)

        viewModel.uiState.test {
            skipItems(1)

            awaitItem().let { state ->
                assertEquals(CustomResult.Error, state.criteriaFetchResult)
                assertEquals(CustomResult.Error, state.eventsSyncResult)
            }
        }
    }
    @Test
    fun onStart_fetchCriteriaAndSchedule_criteriaPresent_fetchResultSuccess() = runTest {
        setup(isCriteriaPresent = true)

        viewModel.uiState.test {
            skipItems(1)

            awaitItem().let { state ->
                assertEquals(CustomResult.Success, state.criteriaFetchResult)
                assertEquals(CustomResult.Success, state.eventsSyncResult)
            }
        }
    }
    @Test
    fun onStart_fetchCriteriaAndSchedule_criteriaPresent_scheduleResultError() = runTest {
        val exception = RuntimeException("schedule exception")
        setup(isCriteriaPresent = true,
            scheduleException = exception)

        launch {
            viewModel.uiEvents.test {
                assertTrue(awaitItem() is DNDCalendarUIEvent.ShowSnackbar)
            }
        }
        viewModel.uiState.test {
            skipItems(1)

            awaitItem().let { state ->
                assertEquals(CustomResult.Success, state.criteriaFetchResult)
                assertEquals(CustomResult.Error, state.eventsSyncResult)
            }
        }
        verify { analyticsMock.recordException(exception) }
    }

    @Test
    fun updateCriteria_criteriaNotPresent_updateNotPerformed() = runTest {
        setup(isCriteriaPresent = false)

        viewModel.uiState.test {
            skipItems(2)

            viewModel.handleIntent(DNDCalendarIntent.Schedule)
            expectNoEvents()
        }
    }
    @Test
    fun updateCriteria_criteriaPresent_updatePerformed() = runTest {
        setup(isCriteriaPresent = true)

        launch {
            viewModel.uiEvents.test {
                assertTrue(awaitItem() is DNDCalendarUIEvent.Unfocus)
                assertTrue(awaitItem() is DNDCalendarUIEvent.ShowSnackbar)
            }
        }
        viewModel.uiState.test {
            skipItems(1)
            awaitItem()
        }
        viewModel.handleIntent(DNDCalendarIntent.Schedule)
        advanceUntilIdle()
        assertEquals(CustomResult.Success, viewModel.uiState.value.eventsSyncResult)
        coVerify(exactly = 1) { dndScheduleCalendarCriteriaManager.changeCriteria(criteria) }
        coVerify(exactly = 2) { dndCalendarScheduler.schedule() }
    }

    @Test
    fun deleteNameLikeCriteria_criteriaDeleted() = runTest {
        setup(isCriteriaPresent = true)

        viewModel.uiState.test {
            skipItems(1)
            assertEquals(awaitItem().criteria, criteria)
            viewModel.apply {
                criteria.likeNames.forEach {
                    handleIntent(DNDCalendarIntent.DeleteCriteria(DNDCalendarCriteriaDeletion.NameLike(it)))
                }
            }
            val updatedCriteria = awaitItem().criteria!!
            assertEquals(criteria.attendees, updatedCriteria.attendees)
            assertTrue(updatedCriteria.likeNames.isEmpty())
            assertFalse(updatedCriteria.isEmpty())
        }
    }
    @Test
    fun deleteAttendeeCriteria_criteriaDeleted() = runTest {
        setup(isCriteriaPresent = true)

        viewModel.uiState.test {
            skipItems(1)
            assertEquals(awaitItem().criteria, criteria)
            viewModel.apply {
                criteria.attendees.forEach {
                    handleIntent(DNDCalendarIntent.DeleteCriteria(DNDCalendarCriteriaDeletion.Participant(it)))
                }
            }
            val updatedCriteria = awaitItem().criteria!!
            assertEquals(criteria.likeNames, updatedCriteria.likeNames)
            assertTrue(updatedCriteria.attendees.isEmpty())
            assertFalse(updatedCriteria.isEmpty())
        }
    }

    @Test
    fun toggleDNDMode_dndOn_doSet() = runTest {
        setup()
        toggleDNDMode_dndOn_helper(set = true)
    }
    @Test
    fun toggleDNDMode_dndOn_doNotSet() = runTest {
        setup()
        toggleDNDMode_dndOn_helper(set = false)
    }
    @Test
    fun toggleDNDMode_dndOn_exception_snackbarShown() = runTest {
        val exception = RuntimeException("")
        setup(toggleDNDModeException = exception)

        launch {
            viewModel.uiEvents.test {
                assertTrue(awaitItem() is DNDCalendarUIEvent.ShowSnackbar)
            }
        }

        toggleDNDMode_dndOn_helper(set = true)
        verify { analyticsMock.recordException(exception) }
    }
    private suspend fun TestScope.toggleDNDMode_dndOn_helper(set: Boolean) {
        viewModel.uiState.test {
            skipItems(2)
        }

        viewModel.handleIntent(DNDCalendarIntent.ToggleDNDOn(id = upcomingEventData.id, set = set))
        advanceUntilIdle()
        if (set) {
            coVerify { upcomingEventsManager.setDndOnToggle(id = upcomingEventData.id, startTime = upcomingEventData.startTime) }
        } else {
            coVerify { upcomingEventsManager.removeDndOnToggle(id = upcomingEventData.id) }
        }
    }
}