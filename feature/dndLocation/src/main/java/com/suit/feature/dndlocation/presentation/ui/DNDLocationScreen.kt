package com.suit.feature.dndlocation.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.GeocodingResult
import com.suit.dndlocation.api.Geometry
import com.suit.dndlocation.api.Properties
import com.suit.feature.dndlocation.R
import com.suit.feature.dndlocation.presentation.DNDLocationUIState
import com.suit.feature.dndlocation.presentation.DNDLocationViewModel
import com.suit.feature.dndlocation.presentation.ui.components.GeocodingResultColumn
import com.suit.feature.dndlocation.presentation.ui.components.LocationConfirmationDialog
import com.suit.feature.dndlocation.presentation.ui.components.LocationPermissionComponent
import com.suit.utility.ui.theme.SilentSyncTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DNDLocationScreen(
    viewModel: DNDLocationViewModel = koinViewModel()
) {
    LocationPermissionComponent {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        DNDLocationScreenContent(
            uiState = uiState,
            onIntent = viewModel::handleIntent
        )
    }
}

@Composable
fun DNDLocationScreenContent(
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
        modifier = Modifier.padding(10.dp)
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
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged {
                        showResultColumn = it.isFocused
                    }
                    .onGloballyPositioned{ coordinates ->
                        textFieldHeight = with(localDensity) { coordinates.size.height.toDp() }
                    }
            )
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
        LocationConfirmationDialog(
            feature = selectedFeature!!,
            onConfirm = { turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue ->
                onIntent(DNDLocationIntent.ConfirmLocation(selectedFeature!!, turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue))
                selectedFeature = null
                        },
            onDismiss = { selectedFeature = null }
        )
    }
}

@Preview
@Composable
fun DNDLocationScreenContentPreview() {
    SilentSyncTheme {
        Surface {
            DNDLocationScreenContent(
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
}