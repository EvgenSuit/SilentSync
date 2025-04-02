package com.suit.feature.dndlocation.presentation.ui

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.suit.dndlocation.impl.LocationService
import com.suit.feature.dndlocation.presentation.ui.components.LocationPermissionComponent

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DNDLocationScreen() {
    LocationPermissionComponent {
        val applicationContext = LocalContext.current.applicationContext
        LaunchedEffect(applicationContext) {
            applicationContext.startForegroundService(
                Intent(applicationContext, LocationService::class.java)
            )
        }

        Text("Showing UI")
    }
}

@Composable
fun DNDLocationScreenContent() {
    
}