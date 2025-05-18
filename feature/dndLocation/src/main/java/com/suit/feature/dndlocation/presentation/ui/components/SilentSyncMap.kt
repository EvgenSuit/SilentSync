package com.suit.feature.dndlocation.presentation.ui.components

import android.animation.ValueAnimator
import android.location.Location
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.Geometry
import com.suit.dndlocation.api.RadiusMeasurement
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.api.SavedLocation
import com.suit.feature.dndlocation.R
import kotlinx.coroutines.launch
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentSyncMap(currLocation: Location?,
                  savedLocations: List<SavedLocation>?,
                  onLocationUpdate: (Feature, TurnDNDOnUponEntering, TurnDNDOffUponExiting, RadiusValue) -> Unit) {
    if (currLocation == null) return

    val scope = rememberCoroutineScope()
    val circlePosition = remember { mutableStateOf(LatLng(currLocation.latitude, currLocation.longitude)) }

    // animation of a circle representing current location
    LaunchedEffect(currLocation) {
        val startPosition = circlePosition.value
        val endPosition = LatLng(currLocation.latitude, currLocation.longitude)

        // Animate from current circle position to new location
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 800
        animator.interpolator = android.view.animation.AccelerateDecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val fraction = animation.animatedValue as Float
            // Linear interpolation between start and end positions
            val lat = startPosition.latitude + (endPosition.latitude - startPosition.latitude) * fraction
            val lng = startPosition.longitude + (endPosition.longitude - startPosition.longitude) * fraction
            circlePosition.value = LatLng(lat, lng)
        }

        animator.start()
    }

    val zoom = 17f
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(currLocation.latitude, currLocation.longitude),
            zoom
        )
    }
    var detailsIndex by rememberSaveable {
        mutableStateOf(-1)
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            mapColorScheme = if (isSystemInDarkTheme()) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            savedLocations?.forEachIndexed { i, targetLocation ->
                val isSelected = detailsIndex == i
                val targetAlpha = if (isSelected) 0.8f else 0.4f
                val animatedAlpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = tween(220)
                )

                val targetStrokeColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.background
                val animatedStrokeColor by animateColorAsState(
                    targetValue = targetStrokeColor,
                    animationSpec = tween(220)
                )
                AdvancedMarker(
                    state = MarkerState(position = LatLng(targetLocation.latitude, targetLocation.longitude)),
                    onClick = {
                        detailsIndex = i
                        true
                    }
                )
                Circle(
                    center = LatLng(targetLocation.latitude, targetLocation.longitude),
                    fillColor = MaterialTheme.colorScheme.primaryContainer.copy(animatedAlpha),
                    strokeColor = animatedStrokeColor,
                    radius = when(targetLocation.radiusMeasurement) {
                        RadiusMeasurement.Meters -> targetLocation.radius
                        RadiusMeasurement.Yards -> targetLocation.radius * 1.09361
                    },
                    clickable = true,
                    onClick = {
                        detailsIndex = i
                        true
                    }
                )
            }
            Circle(
                center = circlePosition.value,
                fillColor = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeColor = MaterialTheme.colorScheme.primaryContainer,
                strokeWidth = 2f,
                radius = 8.0 * 2.0.pow((18.0 - cameraPositionState.position.zoom))
            )
        }
        ElevatedButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(currLocation.latitude, currLocation.longitude), zoom))
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(10.dp)
        ) {
            Icons.Filled.LocationOn.let {
                Icon(it, contentDescription = it.name,
                    modifier = Modifier.padding(6.dp))
            }
        }
    }
    if (detailsIndex != -1 && savedLocations != null) {
        val selectedLocation = savedLocations[detailsIndex]
        val feature = Feature(
            geometry = Geometry(coordinates = listOf(selectedLocation.longitude, selectedLocation.latitude)),
            properties = com.suit.dndlocation.api.Properties(selectedLocation.mapBoxId, fullAddress = selectedLocation.fullAddress)
        )
        LocationConfirmationSheet(
            update = true,
            feature = feature,
            savedLocation = selectedLocation,
            onConfirm = { turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue ->
                onLocationUpdate(feature, turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue)
                detailsIndex = -1
            },
            onDismiss = { detailsIndex = -1 }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailsSheet(
    savedLocation: SavedLocation,
    sheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            modifier = Modifier.fillMaxSize()
                .padding(10.dp)
        ) {
            LocationDetailSection(
                section = R.string.address,
                value = savedLocation.fullAddress
            )
            LocationDetailSection(
                section = R.string.radius,
                value = savedLocation.radius.toString()
            )
        }
    }
}

@Composable
fun LocationDetailSection(
    @StringRes section: Int,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(section),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            ))
        Text(value)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LocationDetailsSheetPreview() {
    LocationDetailsSheet(
        savedLocation = SavedLocation(
            latitude = 51.1,
            longitude = 17.4,
            radius = 100.0,
            radiusMeasurement = com.suit.dndlocation.api.RadiusMeasurement.Meters,
            turnDNDOnUponEntering = true,
            turnDNDOffUponExiting = true,
            didEnter = false,
            didExit = false,
            mapBoxId = "",
            fullAddress = "Wrocław, Lower Silesian Voivodeship, Poland"
        ),
        sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded),
        onDismiss = {} )
}