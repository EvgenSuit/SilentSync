package com.suit.feature.dndcalendar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.feature.dndcalendar.presentation.DNDCalendarUIState
import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarContent
import com.suit.testutil.test.MainDispatcherRule
import com.suit.testutil.test.getString
import com.suit.testutil.test.setContentWithSnackbar
import com.suit.utility.ui.CustomResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DNDCalendarScreenTests {
    @get: Rule
    val composeRule = createComposeRule()
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

    val upcomingEvents = List(10) {
        UpcomingEventData(
            id = it.toLong(),
            title = "Title $it",
            startTime = (it*2).toLong(),
            endTime = ((it*2)+1).toLong()
        )
    }

    @Test
    fun syncEvents_resultSuccess_eventsSyncedTextShown() {
        val uiState = mutableStateOf(DNDCalendarUIState())
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(uiState.value) { } }
        ) {
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
            uiState.value = DNDCalendarUIState(eventsSyncResult = CustomResult.Success)
            onNodeWithText(getString(R.string.events_synced)).assertIsDisplayed()
            onNodeWithText(getString(R.string.events_not_synced)).assertDoesNotExist()
        }
    }
    @Test
    fun syncEvents_resultError_eventsSyncedTextNotShown() {
        val uiState = mutableStateOf(DNDCalendarUIState())
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(uiState.value) { } }
        ) {
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
            uiState.value = DNDCalendarUIState(eventsSyncResult = CustomResult.Error)
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
            onNodeWithText(getString(R.string.events_synced)).assertDoesNotExist()
        }
    }

    @Test
    fun scheduleEvents_criteriaNull_scheduleDisabled() {
        val uiState = mutableStateOf(DNDCalendarUIState())
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(uiState.value) { } }
        ) {
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
            uiState.value = DNDCalendarUIState()
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
        }
    }
    @Test
    fun scheduleEvents_criteriaNotNull_criteriaDisplayed() {
        val uiState = mutableStateOf(DNDCalendarUIState())
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(uiState.value) { } }
        ) {
            val likeName = "event"
            val likeAttendee = "evgen"
            uiState.value = DNDCalendarUIState(criteria = DNDScheduleCalendarCriteria(likeNames = listOf(likeName),
                attendees = listOf(likeAttendee)
            ))
            onNodeWithText(likeName).assertIsDisplayed()
            onNodeWithText(likeAttendee).assertIsDisplayed()
        }
    }

    @Test
    fun upcomingEventsNotEmpty() {
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(DNDCalendarUIState(
                upcomingEvents = upcomingEvents
            ), onIntent = {}) }
        ) {
            upcomingEvents.forEach {
                onNodeWithTag("UpcomingEventsColumn").performScrollToKey(it.id)
                onNodeWithTag("UpcomingEventId: ${it.id}").assertExists()
            }
        }
    }
    @Test
    fun dndOffOverlaps_dndOffOptionDisabled() {
        val upcomingEvents = List(10) {
            UpcomingEventData(
                id = it.toLong(),
                title = "Title $it",
                startTime = (it*2).toLong(),
                endTime = ((it*2)+1).toLong(),
                doesDndOffOverlap = true
            )
        }
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(DNDCalendarUIState(
                upcomingEvents = upcomingEvents
            ), onIntent = {}) }
        ) {
            upcomingEvents.forEach {
                onNodeWithTag("UpcomingEventsColumn").performScrollToKey(it.id)
                onNodeWithTag("DND On: ${it.id}", useUnmergedTree = true).assertIsEnabled()
                onNodeWithTag("DND Off: ${it.id}", useUnmergedTree = true).assertIsNotEnabled()
            }
        }
    }
}