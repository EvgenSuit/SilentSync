package com.suit.feature.dndlocation.presentation.ui

import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.SavedLocation
import com.suit.feature.dndlocation.R
import com.suit.feature.dndlocation.presentation.DNDLocationUIState
import com.suit.feature.dndlocation.presentation.DNDLocationViewModel
import com.suit.feature.dndlocation.presentation.ui.components.GeocodingResultColumn
import com.suit.feature.dndlocation.presentation.ui.components.LocationConfirmationSheet
import com.suit.feature.dndlocation.presentation.ui.components.LocationPermissionComponent
import com.suit.feature.dndlocation.presentation.ui.components.SilentSyncMap
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DNDLocationScreen(
    viewModel: DNDLocationViewModel = koinViewModel()
) {
    LocationPermissionComponent {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val currLocation by viewModel.locationFlow.collectAsState()
        val savedLocations by viewModel.savedLocations.collectAsState()
        val isFeatureEnabled by viewModel.isLocationFeatureEnabled.collectAsState()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            viewModel.handleIntent(DNDLocationIntent.ToggleService(true))
        }
        LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
            viewModel.handleIntent(DNDLocationIntent.ToggleService(false))
        }
        DNDLocationScreenContent(
            currLocation = currLocation,
            isFeatureEnabled = isFeatureEnabled,
            savedLocations = savedLocations,
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DNDLocationScreenContent(
    currLocation: Location?,
    savedLocations: List<SavedLocation>?,
    isFeatureEnabled: Boolean?,
    uiState: DNDLocationUIState,
    onIntent: (DNDLocationIntent) -> Unit
) {
    val geocodingResult = uiState.geocodingResult
    val focusManager = LocalFocusManager.current
    var textFieldHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    var showResultColumn by remember { mutableStateOf(false) }
    var resultsColumnRect by remember { mutableStateOf(Rect.Zero) }

    var selectedFeature by remember { mutableStateOf<Feature?>(null) }
    Box(
        modifier = Modifier.padding(top = 10.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    if (!resultsColumnRect.contains(awaitFirstDown().position)) showResultColumn = false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = uiState.locationInput,
                onValueChange = {
                    onIntent(DNDLocationIntent.LocationInput(it))
                },
                label = {
                    Text(stringResource(R.string.location))
                },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus(true)
                    }
                ),
                trailingIcon = {
                    if (showResultColumn) {
                        IconButton(
                            onClick = {
                                onIntent(DNDLocationIntent.LocationInput(""))
                                focusManager.clearFocus(true)
                            }
                        ) {
                            Icons.Filled.Clear.let {
                                Icon(it, contentDescription = it.name)
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged {
                        showResultColumn = it.isFocused
                    }
                    .onGloballyPositioned{ coordinates ->
                        textFieldHeight = with(localDensity) { coordinates.size.height.toDp() }
                    }
            )
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                SilentSyncMap(
                    currLocation = currLocation,
                    savedLocations = savedLocations,
                    onLocationUpdate = { feature, turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue ->
                        onIntent(DNDLocationIntent.ConfirmLocation(feature, turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue))
                    },
                    onLocationDelete = { id ->
                        onIntent(DNDLocationIntent.DeleteLocation(id))
                    }
                )
                Row(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.background.copy(0.7f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            stringResource(if (isFeatureEnabled == true) R.string.tracking_enabled else R.string.tracking_disabled)
                        )
                        Checkbox(
                            checked = isFeatureEnabled == true,
                            onCheckedChange = {
                                onIntent(DNDLocationIntent.ToggleLocationFeatureAvailability(it))
                            },
                        )
                    }
                }
            }
        }
        AnimatedVisibility(showResultColumn,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GeocodingResultColumn(
                geocodingResult = geocodingResult,
                onClick = {
                    focusManager.clearFocus(true)
                    selectedFeature = it
                },
                modifier = Modifier
                    .padding(top = textFieldHeight + 3.dp)
                    .onGloballyPositioned { coordinates ->
                        resultsColumnRect = coordinates.boundsInRoot()
                    }
            )
        }
    }
    if (selectedFeature != null) {
        LocationConfirmationSheet(
            update = false,
            fullAddress = selectedFeature!!.properties.fullAddress,
            onConfirm = { turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue ->
                onIntent(DNDLocationIntent.ConfirmLocation(selectedFeature!!, turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue))
                selectedFeature = null
                        },
            onDismiss = { selectedFeature = null },
            onDelete = {}
        )
    }
}

/*@Preview
@Composable
fun DNDLocationScreenContentPreview() {
    SilentSyncTheme {
        Surface {
            DNDLocationScreenContent(
                currLocation = Location(""),
                uiState = DNDLocationUIState(
                    locationInput = "Wroclaw",
                    geocodingResult = GeocodingResult(
                        features = List(10) {
                            Feature(
                                geometry = Geometry(coordinates = listOf(51.1, 17.4)),
                                properties = Properties(mapboxId = "", fullAddress = "Wrocław, Lower Silesian Voivodeship, Poland")
                            )
                        }
                    )
                )
            ) { }
        }
    }
}*/
