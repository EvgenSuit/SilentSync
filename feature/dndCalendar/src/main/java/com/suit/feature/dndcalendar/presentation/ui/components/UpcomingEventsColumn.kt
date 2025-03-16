package com.suit.feature.dndcalendar.presentation.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.feature.dndcalendar.R
import com.suit.feature.dndcalendar.presentation.DNDCalendarIntent

@Composable
fun UpcomingEventsColumn(
    isEventSyncInProgress: Boolean,
    upcomingEvents: List<UpcomingEventData>,
    onIntent: (DNDCalendarIntent) -> Unit
) {
    Column {
        Text(
            stringResource(R.string.upcoming_events),
            style = MaterialTheme.typography.titleSmall
        )
        Crossfade(isEventSyncInProgress) { isInProgress ->
            if (isInProgress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .testTag("ProgressIndicator")
                )
            } else Spacer(Modifier.height(5.dp))
        }
        Crossfade(upcomingEvents.isEmpty()) { isEventsListEmpty ->
            if (isEventsListEmpty) {
                Text(stringResource(R.string.nothing_here_yet))
            } else {
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .testTag("UpcomingEventsColumn")
                ) {
                    items(upcomingEvents, key = { it.id }) { event ->
                        UpcomingEventComponent(
                            event = event,
                            onDndOnClick = { id, set ->
                                onIntent(
                                    DNDCalendarIntent.ToggleDNDOn(
                                        id,
                                        set
                                    )
                                )
                            },
                            onDndOffClick = { id, set ->
                                onIntent(
                                    DNDCalendarIntent.ToggleDNDOff(
                                        id,
                                        set
                                    )
                                )
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}