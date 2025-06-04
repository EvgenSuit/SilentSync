package com.suit.feature.dndlocation.presentation.ui.components

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
import com.google.android.gms.maps.model.PointOfInterest
import com.google.maps.android.compose.AdvancedMarker
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.suit.dndlocation.api.Feature
import com.suit.dndlocation.api.Geometry
import com.suit.dndlocation.api.Properties
import com.suit.dndlocation.api.RadiusValue
import com.suit.dndlocation.api.SavedLocation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilentSyncMap(currLocation: Location?,
                  savedLocations: List<SavedLocation>?,
                  onLocationUpdate: (Feature, TurnDNDOnUponEntering, TurnDNDOffUponExiting, RadiusValue) -> Unit,
                  onLocationDelete: (Long) -> Unit) {
    if (currLocation == null) return

    val animDuration = 220
    val zoom = 17f
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(currLocation.latitude, currLocation.longitude),
            zoom
        )
    }
    val scope = rememberCoroutineScope()
    var detailsIndex by rememberSaveable {
        mutableStateOf(-1)
    }
    var pointOfInterest by remember {
        mutableStateOf<PointOfInterest?>(null)
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
                compassEnabled = false,
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = true,
                isBuildingEnabled = true
            ),
            onPOIClick = { poi ->
                pointOfInterest = poi
            },
            modifier = Modifier.fillMaxSize().alpha(mapAlpha)
        ) {
            savedLocations?.forEachIndexed { i, targetLocation ->
                val isUserInside = targetLocation.didEnter

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
                    state = MarkerState(
                        position = LatLng(
                            targetLocation.latitude,
                            targetLocation.longitude
                        )
                    ),
                    onClick = {
                        detailsIndex = i
                        true
                    }
                )
                Circle(
                    center = LatLng(targetLocation.latitude, targetLocation.longitude),
                    fillColor = MaterialTheme.colorScheme.primaryContainer.copy(animatedAlpha),
                    strokeColor = animatedStrokeColor,
                    radius = targetLocation.radiusValueMeters(),
                    strokeWidth = animatedStrokeWidth,
                    clickable = true,
                    onClick = {
                        detailsIndex = i
                        true
                    }
                )
            }
        }
        ElevatedButton(
            onClick = {
                scope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                currLocation.latitude,
                                currLocation.longitude
                            ), cameraPositionState.position.zoom
                        )
                    )
                }
            },
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(10.dp)
        ) {
            Icons.Filled.LocationOn.let {
                Icon(
                    it, contentDescription = it.name,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
        AnimatedVisibility(
            !mapLoaded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        if ((detailsIndex != -1 && savedLocations != null) || pointOfInterest != null) {
            val selectedLocation =
                if (savedLocations != null && pointOfInterest == null) savedLocations[detailsIndex] else null
            val feature = Feature(
                geometry = Geometry(
                    coordinates = if (pointOfInterest != null) listOf(
                        pointOfInterest!!.latLng.longitude,
                        pointOfInterest!!.latLng.latitude
                    ) else listOf(selectedLocation!!.longitude, selectedLocation.latitude)
                ),
                properties = if (pointOfInterest != null) Properties(
                    mapboxId = "",
                    fullAddress = pointOfInterest!!.name
                ) else Properties(mapboxId = "", fullAddress = selectedLocation!!.fullAddress)
            )
            LocationConfirmationSheet(
                update = pointOfInterest == null,
                fullAddress = feature.properties.fullAddress,
                savedLocation = selectedLocation,
                onConfirm = { turnDNDOnUponEntering, turnDNDOffUponExiting, radiusValue ->
                    onLocationUpdate(
                        feature,
                        turnDNDOnUponEntering,
                        turnDNDOffUponExiting,
                        radiusValue
                    )
                    if (pointOfInterest != null) pointOfInterest = null else detailsIndex = -1
                },
                onDelete = {
                    onLocationDelete(selectedLocation!!.id)
                },
                onDismiss = {
                    if (pointOfInterest != null) pointOfInterest = null else detailsIndex = -1
                }
            )
        }
    }
}