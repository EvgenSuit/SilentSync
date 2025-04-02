package com.suit.feature.dndlocation.presentation.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.suit.utility.ui.PermissionDialog
import com.suit.utility.ui.navigateToSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionComponent(
    onGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(listOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ))
    if (!permissionsState.allPermissionsGranted) {
        PermissionDialog(
            text = "Access to location is required",
            onAccept = {
                if (permissionsState.shouldShowRationale) context.navigateToSettings()
                else permissionsState.launchMultiplePermissionRequest();
            },
            onDismiss = {}
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val backgroundLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (backgroundLocationPermissionState.status != PermissionStatus.Granted) {
            PermissionDialog(
                text = "Access to background location is required",
                onAccept = {
                    backgroundLocationPermissionState.launchPermissionRequest();
                },
                onDismiss = {}
            )
        } else onGranted()
    } else onGranted()
}