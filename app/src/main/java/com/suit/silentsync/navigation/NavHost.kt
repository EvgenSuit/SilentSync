package com.suit.silentsync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarScreen
import com.suit.feature.dndlocation.presentation.ui.DNDLocationScreen
import kotlinx.serialization.Serializable

sealed class Destination {
    @Serializable
    object DNDCalendar

    @Serializable
    object DNDLocation
}
@Composable
fun SilentSyncNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
) {
    NavHost(
        startDestination = Destination.DNDLocation,
        navController = navController,
        modifier = modifier
    ) {
        composable<Destination.DNDCalendar> {
            DNDCalendarScreen()
        }
        composable<Destination.DNDLocation> {
            DNDLocationScreen()
        }
    }
}