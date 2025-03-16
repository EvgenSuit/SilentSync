package com.suit.feature.dndcalendar.presentation.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.suit.feature.dndcalendar.R
import com.suit.utility.ui.CustomResult

@Composable
fun EventsSyncStatusComponent(
    eventsSyncResult: CustomResult
) {
    Crossfade(eventsSyncResult) { result ->
        Text(
            text = stringResource(if (result.isSuccess()) R.string.events_synced else R.string.events_not_synced),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(
                    alpha = if (result.isInProgress()) 0.35f else 0.8f
                )
            )
        )
    }
}