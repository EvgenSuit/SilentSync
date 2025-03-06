package com.suit.silentsync.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarScreen
import kotlinx.serialization.Serializable

@Serializable
object DNDCalendar

@Composable
fun SilentSyncNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
) {
    NavHost(
        startDestination = DNDCalendar,
        navController = navController,
        modifier = modifier
    ) {
        composable<DNDCalendar> {
            DNDCalendarScreen()
        }
    }
}