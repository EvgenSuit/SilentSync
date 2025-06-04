package com.suit.feature.dndlocation.presentation.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.suit.feature.dndlocation.R
import com.suit.utility.ui.PermissionDialog
import com.suit.utility.ui.navigateToSettings

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionComponent(
    onGranted: @Composable () -> Unit
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOfNotNull(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    if (!permissionsState.allPermissionsGranted) {
        PermissionDialog(
            text = stringResource(R.string.location_permission),
            onAccept = {
                if (permissionsState.shouldShowRationale) context.navigateToSettings()
                else permissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {}
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val backgroundLocationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if (!backgroundLocationPermissionState.status.isGranted) {
            PermissionDialog(
                text = stringResource(R.string.background_location_permission),
                onAccept = {
                    backgroundLocationPermissionState.launchPermissionRequest()
                },
                onDismiss = {}
            )
        } else onGranted()
    }
    else onGranted()
}