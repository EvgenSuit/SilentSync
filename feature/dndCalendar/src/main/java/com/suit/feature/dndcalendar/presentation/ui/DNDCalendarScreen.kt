package com.suit.feature.dndcalendar.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.suit.feature.dndcalendar.R
import com.suit.feature.dndcalendar.presentation.DNDCalendarIntent
import com.suit.feature.dndcalendar.presentation.DNDCalendarUIState
import com.suit.feature.dndcalendar.presentation.DNDCalendarViewModel
import com.suit.utility.ui.CommonButton
import com.suit.utility.ui.theme.SilentSyncTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DNDCalendarScreen(
    viewModel: DNDCalendarViewModel = koinViewModel()
) {
    var showUI by remember {
        mutableStateOf(false)
    }
    DNDPermissionComponent { showUI = true }

    if (showUI) {
        val uiState by viewModel.uiState.collectAsState()
        DNDCalendarContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
    }
}

@Composable
fun DNDCalendarContent(
    uiState: DNDCalendarUIState,
    onIntent: (DNDCalendarIntent) -> Unit
) {
    LaunchedEffect(Unit) {
        onIntent(DNDCalendarIntent.ScheduleDND)
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.areEventsSynced) {
            Text(
                text = stringResource(R.string.events_synced),
                style = MaterialTheme.typography.labelMedium
            )
        }
        CommonButton(
            text = stringResource(R.string.sync_events),
            onClick = { onIntent(DNDCalendarIntent.ScheduleDND) }
        )
    }
}

@Preview
@Composable
fun DNDCalendarContentPreview() {
    SilentSyncTheme {
        Surface {
            DNDCalendarContent(
                uiState = DNDCalendarUIState(
                    areEventsSynced = true
                ),
                onIntent = {}
            )
        }
    }
}