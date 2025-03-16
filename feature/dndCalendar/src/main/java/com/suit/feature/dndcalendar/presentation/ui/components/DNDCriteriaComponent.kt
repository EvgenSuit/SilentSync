package com.suit.feature.dndcalendar.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.suit.feature.dndcalendar.R
import com.suit.utility.ui.theme.SilentSyncTheme
import kotlinx.coroutines.launch

@Composable
fun DNDCriteriaComponent(
    @StringRes labelId: Int,
    criteria: List<String>?,
    onInput: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val maxCriteriaLength = integerResource(R.integer.max_criteria_length)
    var currCriteriaValue by rememberSaveable {
        mutableStateOf("")
    }
    val lazyRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                currCriteriaValue,
                onValueChange = {
                    if (it.length <= maxCriteriaLength) {
                        currCriteriaValue = it
                    }
                },
                supportingText = {
                    if (currCriteriaValue.isNotBlank()) {
                        Box(Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd) {
                            Text("${currCriteriaValue.length}/$maxCriteriaLength")
                        }
                    }
                },
                label = {
                    Text(
                        stringResource(labelId)
                    )
                },
                trailingIcon = {
                    if (currCriteriaValue.isNotBlank() &&
                        (criteria.isNullOrEmpty() || !criteria.contains(currCriteriaValue.trim()))) {
                        CriteriaPreview(
                            onClick = {
                                scope.launch {
                                    onInput(currCriteriaValue)
                                    currCriteriaValue = ""

                                    val lastItemIndex = lazyRowState.layoutInfo.totalItemsCount-1
                                    if (lastItemIndex > 1) lazyRowState.animateScrollToItem(lastItemIndex)
                                }
                            }
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (!criteria.isNullOrEmpty()) {
                LazyRow(
                    state = lazyRowState,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp,
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondary)
                ) {
                    items(criteria) {
                        CriteriaComponent(
                            criteria = it,
                            onDelete = { onDelete(it) },
                            modifier = Modifier
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CriteriaComponent(
    criteria: String,
    onDelete: () -> Unit,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(criteria,
            )
        IconButton(
            onClick = onDelete
        ) {
            val icon = Icons.Filled.Clear
            Icon(icon,
                contentDescription = icon.name)
        }
    }
}

@Composable
fun CriteriaPreview(
    onClick: () -> Unit
) {
    val icon = Icons.Filled.PlayArrow
    IconButton(
        onClick = onClick
    ) {
        Icon(icon,
            contentDescription = icon.name
        )
    }
}

@Preview
@Composable
fun DNDCriteriaComponentPreview() {
    SilentSyncTheme {
        Surface {
            DNDCriteriaComponent(
                labelId = R.string.like_name_criteria,
                criteria = List(3) {
                    "Name $it"
                },
                onDelete = {},
                onInput = {},
            )
        }
    }
}