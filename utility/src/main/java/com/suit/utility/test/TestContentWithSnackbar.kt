package com.suit.utility.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.core.app.ApplicationProvider
import com.suit.utility.ui.CustomSnackbar
import com.suit.utility.ui.SnackbarController
import kotlinx.coroutines.test.TestScope

fun ComposeContentTestRule.setContentWithSnackbar(
    composable: @Composable () -> Unit,
    testBody: ComposeContentTestRule.() -> Unit
) {
    setContent {
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarController by remember(snackbarHostState) {
            mutableStateOf(SnackbarController(
                context = ApplicationProvider.getApplicationContext(),
                snackbarHostState = snackbarHostState
            ))
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) {
                    CustomSnackbar(
                        message = snackbarHostState.currentSnackbarData?.visuals?.message,
                        onDismiss = { snackbarController.dismiss() }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()
                .padding(innerPadding)) {
                composable()
            }
        }
    }
    testBody()
}