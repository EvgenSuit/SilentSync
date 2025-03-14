package com.suit.feature.dndcalendar.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suit.dndcalendar.api.UpcomingEventData
import com.suit.feature.dndcalendar.R
import com.suit.utility.ui.theme.SilentSyncTheme

@Composable
fun UpcomingEventComponent(
    event: UpcomingEventData,
    onDndOnClick: (Long, Boolean) -> Unit,
    onDndOffClick: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
    ) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Text(event.title,
                style = MaterialTheme.typography.titleSmall)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                UpcomingEventCheckbox(
                    id = R.string.turn_dnd_on,
                    checked = event.scheduleDndOn,
                    onClick = { onDndOnClick(event.id, it) }
                )
                UpcomingEventCheckbox(
                    id = R.string.turn_dnd_off,
                    checked = event.scheduleDndOff,
                    onClick = { onDndOffClick(event.id, it) }
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventCheckbox(
    @StringRes id: Int,
    checked: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id))
        Checkbox(
            checked = checked,
            onCheckedChange = onClick
        )
    }
}

@Preview
@Composable
fun UpcomingEventComponentPreview() {
    SilentSyncTheme {
        UpcomingEventComponent(
            event = UpcomingEventData(
                id = 0,
                title = "Some event",
                startTime = 0,
                endTime = 0
            ),
            onDndOnClick = {_, _ -> },
            onDndOffClick = {_, _ -> }
        )
    }
}