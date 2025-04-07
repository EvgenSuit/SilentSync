package com.suit.feature.dndlocation.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.Geometry
import com.suit.dndlocation.api.Properties
import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.RadiusValue
import com.suit.feature.dndlocation.R
import com.suit.utility.ui.theme.SilentSyncTheme
import java.util.Locale

typealias TurnDNDOnUponEntering = Boolean
typealias TurnDNDOffUponExiting = Boolean

@Composable
fun LocationConfirmationDialog(
    feature: Feature,
    onConfirm: (TurnDNDOnUponEntering, TurnDNDOffUponExiting, RadiusValue) -> Unit,
    onDismiss: () -> Unit
) {
    var turnDNDOnUponEntering by rememberSaveable { mutableStateOf(true) }
    var turnDNDOffUponExiting by rememberSaveable { mutableStateOf(true) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var isMeasurementDropdownExpanded by remember { mutableStateOf(false) }
    var radius by remember { mutableStateOf<RadiusValue?>(null) }
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card {
            Column(
                verticalArrangement = Arrangement.spacedBy(35.dp),
                modifier = Modifier
                    .width(450.dp)
                    .padding(20.dp)
            ) {
                Text(feature.properties.fullAddress,
                    style = MaterialTheme.typography.titleSmall)
                RadiusOptions(
                    selectedRadiusValue = radius?.value,
                    isValuesDropdownExpanded = isDropdownExpanded,
                    isMeasurementDropdownExpanded = isMeasurementDropdownExpanded,
                    onValuesDropdownExpandedChange = { isDropdownExpanded = it },
                    onMeasurementDropdownExpandedChange = { isMeasurementDropdownExpanded = it },
                    onRadiusSelect = { value, measurement -> radius = RadiusValue(value, measurement) },
                )
                DNDOptionsRow(
                    turnDNDOnUponEntering = turnDNDOnUponEntering,
                    turnDNDOffUponExiting = turnDNDOffUponExiting,
                    onTurnDNDOnChange = { turnDNDOnUponEntering = it },
                    onTurnDNDOffChange = { turnDNDOffUponExiting = it }
                )
                Box(Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center) {
                    ElevatedButton(
                        onClick = { onConfirm(turnDNDOnUponEntering, turnDNDOffUponExiting, radius!!) }
                    ) {
                        Text(stringResource(R.string.add_location))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadiusOptions(
    selectedRadiusValue: Int?,
    isValuesDropdownExpanded: Boolean,
    isMeasurementDropdownExpanded: Boolean,
    onValuesDropdownExpandedChange: (Boolean) -> Unit,
    onMeasurementDropdownExpandedChange: (Boolean) -> Unit,
    onRadiusSelect: (Int, RadiusMeasurement) -> Unit
) {
    val locale = Locale.getDefault().country
    val radiusInts = remember { generateSequence(10) { (it * 2).toInt() }
        .takeWhile { it <= 2000 }
        .toList() }
    var radiusMeasurement by remember(locale) { mutableStateOf(
        if (locale == "US") RadiusMeasurement.Yards
        else RadiusMeasurement.Meters
    ) }
    LaunchedEffect(Unit) {
        onRadiusSelect(radiusInts[0], radiusMeasurement)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.radius))
        ExposedDropdownMenuBox(
            expanded = isValuesDropdownExpanded,
            onExpandedChange = { onValuesDropdownExpandedChange(it) },
            modifier = Modifier
                .clickable {
                    onValuesDropdownExpandedChange(!isValuesDropdownExpanded)
                }
                .weight(1f),
        ) {
            TextField(
                value = (selectedRadiusValue ?: radiusInts[0]).toString(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = isValuesDropdownExpanded,
                onDismissRequest = { onValuesDropdownExpandedChange(false) }
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    radiusInts.forEach { radius ->
                        DropdownMenuItem(
                            text = { Text(radius.toString()) },
                            onClick = {
                                onValuesDropdownExpandedChange(false)
                                onRadiusSelect(radius, radiusMeasurement) }
                        )
                    }
                }
            }
        }
        ExposedDropdownMenuBox(
            expanded = isMeasurementDropdownExpanded,
            onExpandedChange = { onMeasurementDropdownExpandedChange(it) },
            modifier = Modifier
                .clickable {
                    onMeasurementDropdownExpandedChange(!isMeasurementDropdownExpanded)
                }
                .weight(1f),
        ) {
            TextField(
                value = radiusMeasurement.toString(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            )
            ExposedDropdownMenu(
                expanded = isMeasurementDropdownExpanded,
                onDismissRequest = { onMeasurementDropdownExpandedChange(false) }
            ) {
                listOf(RadiusMeasurement.Meters, RadiusMeasurement.Yards).forEach { measurement ->
                    DropdownMenuItem(
                        text = { Text(measurement.toString()) },
                        onClick = {
                            onMeasurementDropdownExpandedChange(false)
                            radiusMeasurement = measurement }
                    )
                }
            }
        }
    }
}

@Composable
private fun DNDOptionsRow(
    turnDNDOnUponEntering: Boolean,
    turnDNDOffUponExiting: Boolean,
    onTurnDNDOnChange: (Boolean) -> Unit,
    onTurnDNDOffChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        DNDOption(
            id = R.string.turn_dnd_on,
            checked = turnDNDOnUponEntering,
            onCheck = onTurnDNDOnChange,
            modifier = Modifier.testTag("DND ON")
        )
        DNDOption(
            id = R.string.turn_dnd_off,
            checked = turnDNDOffUponExiting,
            onCheck = onTurnDNDOffChange,
            modifier = Modifier.testTag("DND OFF")
        )
    }
}

@Composable
private fun RowScope.DNDOption(
    @StringRes id: Int,
    checked: Boolean,
    onCheck: (Boolean) -> Unit,
    modifier: Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .weight(1f)
    ) {
        Text(stringResource(id),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.weight(1f))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheck
        )
    }
}

@Preview
@Composable
private fun RadiusOptionsPreview() {
    SilentSyncTheme {
        Surface {
            Box(Modifier.fillMaxSize()) {
                RadiusOptions(
                    selectedRadiusValue = null,
                    isValuesDropdownExpanded = true,
                    isMeasurementDropdownExpanded = false,
                    onRadiusSelect = {_, _ -> },
                    onValuesDropdownExpandedChange = {},
                    onMeasurementDropdownExpandedChange = {}
                )
            }
        }
    }
}

@Preview
@Composable
fun LocationConfirmationDialogPreview() {
    SilentSyncTheme {
        Surface {
            LocationConfirmationDialog(
                feature = Feature(
                    geometry = Geometry(
                        coordinates = listOf()
                    ),
                    properties = Properties(
                        mapboxId = "",
                        fullAddress = "Wrocław, Lower Silesian Voivodeship, Poland"
                    )
                ),
                onDismiss = {},
                onConfirm = {_, _, _ -> }
            )
        }
    }
}