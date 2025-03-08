package com.suit.feature.dndcalendar.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suit.dndcalendar.api.DNDScheduleCalendarCriteria
import com.suit.feature.dndcalendar.R
import com.suit.feature.dndcalendar.presentation.DNDCalendarCriteriaInput
import com.suit.feature.dndcalendar.presentation.DNDCalendarIntent
import com.suit.utility.ui.CommonButton
import com.suit.utility.ui.theme.SilentSyncTheme

@Composable
fun DNDCriteriaComponent(
    criteria: DNDScheduleCalendarCriteria?,
    onInput: (DNDCalendarCriteriaInput) -> Unit,
    onSync: () -> Unit
) {
    val maxLikeNameCriteriaLength = integerResource(R.integer.max_like_name_criteria_length)
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                criteria?.likeName ?: "",
                onValueChange = {
                    if (it.length <= maxLikeNameCriteriaLength) {
                        onInput(DNDCalendarCriteriaInput.NameLike(it))
                    }
                },
                supportingText = {
                    criteria?.likeName?.let {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd) {
                            Text("${it.length}/$maxLikeNameCriteriaLength")
                        }
                    }
                },
                label = {
                    Text(
                        stringResource(R.string.like_name_criteria_placeholder)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        CommonButton(
            text = stringResource(R.string.sync_events),
            enabled = criteria != null,
            onClick = onSync
        )
    }
}

@Preview
@Composable
fun DNDCriteriaComponentPreview() {
    SilentSyncTheme {
        Surface {
            DNDCriteriaComponent(
                criteria = DNDScheduleCalendarCriteria("Something".repeat(19)),
                onInput = {},
                onSync = {}
            )
        }
    }
}