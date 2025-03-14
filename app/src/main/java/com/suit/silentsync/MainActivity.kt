package com.suit.silentsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import com.suit.silentsync.navigation.SilentSyncNavHost
import com.suit.utility.ui.CustomSnackbar
import com.suit.utility.ui.LocalSnackbarController
import com.suit.utility.ui.SnackbarController
import com.suit.utility.ui.theme.SilentSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarController by remember(snackbarHostState) {
                mutableStateOf(SnackbarController(
                    context = applicationContext,
                    snackbarHostState = snackbarHostState
                ))
            }
            val focusManager = LocalFocusManager.current
            SilentSyncTheme {
                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) {
                            CustomSnackbar(message = snackbarHostState.currentSnackbarData?.visuals?.message,
                                onDismiss = snackbarController::dismiss)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val downEvent = awaitFirstDown(pass = PointerEventPass.Initial)
                                val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                if (upEvent != null && !downEvent.isConsumed) focusManager.clearFocus(true)
                            }
                        }) { innerPadding ->
                    CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
                        SilentSyncNavHost(
                            modifier = Modifier
                                .padding(innerPadding)
                                // prevent padding above keyword applied by innerPadding
                                .consumeWindowInsets(innerPadding)
                                // apply padding when keyboard appears
                                .windowInsetsPadding(WindowInsets.ime)
                        )
                    }
                }
            }
        }
    }
}

