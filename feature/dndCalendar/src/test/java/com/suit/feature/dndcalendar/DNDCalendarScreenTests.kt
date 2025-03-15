package com.suit.feature.dndcalendar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToKey
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.feature.dndcalendar.presentation.DNDCalendarUIState
import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarContent
import com.suit.utility.test.MainDispatcherRule
import com.suit.utility.test.getString
import com.suit.utility.test.setContentWithSnackbar
import com.suit.utility.ui.CustomResult
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DNDCalendarScreenTests {
    @get: Rule
    val composeRule = createComposeRule()
    @get: Rule
    val mainDispatcherRule = MainDispatcherRule()

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
            onNodeWithText(getString(R.string.sync_events)).assertIsNotEnabled()
            uiState.value = DNDCalendarUIState()
            onNodeWithText(getString(R.string.sync_events)).assertIsNotEnabled()
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
        }
    }
    @Test
    fun scheduleEvents_criteriaNotNull_scheduleEnabled() {
        val uiState = mutableStateOf(DNDCalendarUIState())
        composeRule.setContentWithSnackbar(
            composable = { DNDCalendarContent(uiState.value) { } }
        ) {
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
            onNodeWithText(getString(R.string.sync_events)).assertIsNotEnabled()
            uiState.value = DNDCalendarUIState(criteria = DNDScheduleCalendarCriteria(likeName = "event"))

            onNodeWithText(getString(R.string.sync_events)).assertIsEnabled()
            onNodeWithText(getString(R.string.events_not_synced)).assertIsDisplayed()
        }
    }

    @Test
    fun upcomingEventsNotEmpty() {
        val upcomingEvents = List(10) {
            UpcomingEventData(
                id = it.toLong(),
                title = "Title $it",
                startTime = (it*2).toLong(),
                endTime = ((it*2)+1).toLong()
            )
        }
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
}