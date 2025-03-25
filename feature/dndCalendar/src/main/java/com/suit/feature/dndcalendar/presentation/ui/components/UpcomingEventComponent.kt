package com.suit.feature.dndcalendar.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
    print(event)
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
            .testTag("UpcomingEventId: ${event.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            Text(event.title,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UpcomingEventCheckbox(
                    stringId = R.string.turn_dnd_on,
                    checked = event.scheduleDndOn,
                    onClick = { onDndOnClick(event.id, it) },
                    modifier = Modifier.weight(1f),
                    checkboxTestTag = "DND On: ${event.id}"
                )
                UpcomingEventCheckbox(
                    stringId = R.string.turn_dnd_off,
                    enabled = !event.doesDndOffOverlap,
                    checked = event.scheduleDndOff && !event.doesDndOffOverlap,
                    onClick = { onDndOffClick(event.id, it) },
                    modifier = Modifier.weight(1f),
                    checkboxTestTag = "DND Off: ${event.id}"
                )
            }
        }
    }
}

@Composable
private fun UpcomingEventCheckbox(
    @StringRes stringId: Int,
    checked: Boolean,
    enabled: Boolean = true,
    onClick: (Boolean) -> Unit,
    modifier: Modifier,
    checkboxTestTag: String
) {
    print(checkboxTestTag)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            stringResource(stringId),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall
        )
        Checkbox(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onClick,
            modifier = Modifier.testTag(checkboxTestTag)
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
                title = "Some event".repeat(15),
                startTime = 0,
                endTime = 0
            ),
            onDndOnClick = {_, _ -> },
            onDndOffClick = {_, _ -> }
        )
    }
}