package com.suit.feature.dndlocation.presentation.ui.components

import android.animation.ValueAnimator
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.pow

fun isUserInZone(userLocation: Location, zoneLatLng: LatLng, radiusMeters: Double): Boolean =
    userLocation.distanceTo(Location("").apply {
        latitude = zoneLatLng.latitude
        longitude = zoneLatLng.longitude
    }) <= radiusMeters

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
    val animDuration = 220
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(currLocation.latitude, currLocation.longitude),
            zoom
        )
    }
    var detailsIndex by rememberSaveable {
        mutableStateOf(-1)
    }
    val userInsideZoneIndex = remember(savedLocations, currLocation) {
        savedLocations?.mapIndexedNotNull { index, location ->
            val radiusMeters = when (location.radiusMeasurement) {
                RadiusMeasurement.Meters -> location.radius
                RadiusMeasurement.Yards -> location.radius * 0.9144
            }
            val zoneLatLng = LatLng(location.latitude, location.longitude)
            if (isUserInZone(currLocation, zoneLatLng, radiusMeters)) index to radiusMeters else null
        }
            ?.minByOrNull { it.second } // Select smallest radius among matching zones
            ?.first
    }
    var mapLoaded by remember { mutableStateOf(false) }
    val mapAlpha by animateFloatAsState(
        targetValue = if (mapLoaded) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "mapAlpha"
    )
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            cameraPositionState = cameraPositionState,
            onMapLoaded = { mapLoaded = true },
            mapColorScheme = if (isSystemInDarkTheme()) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            ),
            modifier = Modifier.fillMaxSize().alpha(mapAlpha)
        ) {
            savedLocations?.forEachIndexed { i, targetLocation ->
                val isUserInside = userInsideZoneIndex == i

                val isSelected = detailsIndex == i
                val targetAlpha = when {
                    isSelected -> 0.3f
                    isUserInside -> 0.8f
                    else -> 0.4f
                }
                val animatedAlpha by animateFloatAsState(
                    targetValue = targetAlpha,
                    animationSpec = tween(animDuration)
                )

                val targetStrokeColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    isUserInside -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.background
                }
                val animatedStrokeColor by animateColorAsState(
                    targetValue = targetStrokeColor,
                    animationSpec = tween(animDuration)
                )

                val targetStrokeWidth = when {
                    isSelected -> 2f
                    isUserInside -> 12f
                    else -> 8f
                }
                val animatedStrokeWidth by animateFloatAsState(
                    targetValue = targetStrokeWidth,
                    animationSpec = tween(animDuration)
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
                    strokeWidth = animatedStrokeWidth,
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
        AnimatedVisibility(!mapLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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