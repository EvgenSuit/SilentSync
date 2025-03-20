package com.suit.feature.dndcalendar.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.feature.dndcalendar.R
import com.suit.feature.dndcalendar.presentation.DNDCalendarCriteriaDeletion
import com.suit.feature.dndcalendar.presentation.DNDCalendarCriteriaInput
import com.suit.feature.dndcalendar.presentation.DNDCalendarIntent
import com.suit.feature.dndcalendar.presentation.DNDCalendarUIState
import com.suit.feature.dndcalendar.presentation.DNDCalendarViewModel
import com.suit.feature.dndcalendar.presentation.ui.components.DNDCriteriaComponent
import com.suit.feature.dndcalendar.presentation.ui.components.DNDPermissionComponent
import com.suit.feature.dndcalendar.presentation.ui.components.EventsSyncStatusComponent
import com.suit.feature.dndcalendar.presentation.ui.components.UpcomingEventsColumn
import com.suit.utility.ui.CommonButton
import com.suit.utility.ui.CustomResult
import com.suit.utility.ui.DNDCalendarUIEvent
import com.suit.utility.ui.LocalSnackbarController
import com.suit.utility.ui.theme.SilentSyncTheme
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun DNDCalendarScreen(
    viewModel: DNDCalendarViewModel = koinViewModel()
) {
    var showUI by remember {
        mutableStateOf(false)
    }
    DNDPermissionComponent { showUI = true }

    AnimatedVisibility(
        showUI,
        enter = fadeIn()
    ) {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val focusManager = LocalFocusManager.current
        val snackbarController = LocalSnackbarController.current
        LaunchedEffect(viewModel) {
            viewModel.uiEvents.collectLatest { event ->
                when (event) {
                    is DNDCalendarUIEvent.ShowSnackbar -> snackbarController.showSnackbar(event.uiText)
                    is DNDCalendarUIEvent.Unfocus -> focusManager.clearFocus(true)
                }
            }
        }

        DNDCalendarContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DNDCalendarContent(
    uiState: DNDCalendarUIState,
    onIntent: (DNDCalendarIntent) -> Unit
) {
    // TODO remove this refresh box
    PullToRefreshBox(
        isRefreshing = uiState.criteriaFetchResult.isInProgress(),
        onRefresh = { onIntent(DNDCalendarIntent.GetCriteria) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            EventsSyncStatusComponent(
                eventsSyncResult = uiState.eventsSyncResult
            )
            Spacer(Modifier.height(27.dp))
            Text(
                stringResource(R.string.set_dnd_toggle_criteria_prompt),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(10.dp))
            DNDCriteriaComponent(
                labelId = R.string.like_name_criteria,
                criteria = uiState.criteria?.likeNames,
                onInput = { onIntent(DNDCalendarIntent.InputCriteria(
                    DNDCalendarCriteriaInput.NameLike(it)
                )) },
                onDelete = {
                    onIntent(DNDCalendarIntent.DeleteCriteria(
                        DNDCalendarCriteriaDeletion.NameLike(it)
                    ))
                }
            )
            DNDCriteriaComponent(
                labelId = R.string.participants,
                criteria = uiState.criteria?.attendees,
                onInput = { onIntent(DNDCalendarIntent.InputCriteria(
                    DNDCalendarCriteriaInput.Participant(it)
                )) },
                onDelete = {
                    onIntent(DNDCalendarIntent.DeleteCriteria(
                        DNDCalendarCriteriaDeletion.Participant(it)
                    ))
                }
            )
            CommonButton(
                text = stringResource(R.string.sync_events),
                onClick = { onIntent(DNDCalendarIntent.Schedule) }
            )

            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HorizontalDivider(Modifier.fillMaxWidth(0.5f))
            }

            Spacer(Modifier.height(10.dp))
            UpcomingEventsColumn(
                isEventSyncInProgress = uiState.eventsSyncResult.isInProgress(),
                upcomingEvents = uiState.upcomingEvents,
                onIntent = onIntent
            )
        }
    }
}

@Preview
@Composable
fun DNDCalendarContentPreview() {
    SilentSyncTheme {
        Surface {
            DNDCalendarContent(
                uiState = DNDCalendarUIState(
                    eventsSyncResult = CustomResult.Success,
                    upcomingEvents = /*listOf()*/ List(50) {
                        UpcomingEventData(
                            id = it.toLong(),
                            title = "Title $it",
                            startTime = 0,
                            endTime = 0
                        )
                    },
                    criteria = DNDScheduleCalendarCriteria(
                        likeNames = listOf(),
                        attendees = listOf("jake")
                    )
                ),
                onIntent = {}
            )
        }
    }
}