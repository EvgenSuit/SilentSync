package com.suit.silentsync.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.suit.feature.dndcalendar.presentation.ui.DNDCalendarScreen
import com.suit.feature.dndlocation.presentation.ui.DNDLocationScreen
import kotlinx.serialization.Serializable

sealed class Destination {
    @Serializable
    object DNDCalendar: Destination()
    @Serializable
    object DNDLocation: Destination()

    fun toRouteString() = this::class.qualifiedName
}
data class BottomBarItem(
    val destination: Destination,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SilentSyncNavHost(
    navController: NavHostController,
    modifier: Modifier
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            startDestination = Destination.DNDCalendar,
            navController = navController,
            exitTransition = { ExitTransition.None },
            enterTransition = { EnterTransition.None },
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
}

@Composable
fun BottomBar(
    currentEntry: NavBackStackEntry?,
    onNavigate: (Destination) -> Unit
) {
    val bottomBarItems = listOf(
        BottomBarItem(
            destination = Destination.DNDCalendar,
            label = "Calendar",
            icon = Icons.Default.DateRange
        ),
        BottomBarItem(
            destination = Destination.DNDLocation,
            label = "Location",
            icon = Icons.Default.LocationOn
        )
    )
    NavigationBar {
        bottomBarItems.forEach { screen ->
            NavigationBarItem(
                label = {},
                onClick = {
                    // mitigate changes of Google Map going blank on rapid navigation
                    if (currentEntry?.destination?.route != screen.destination.toRouteString()) {
                        onNavigate(screen.destination)
                    }
                },
                selected = currentEntry?.destination?.route == screen.destination.toRouteString(),
                icon = {
                    Icon(screen.icon, contentDescription = screen.icon.name)
                },
            )
        }
    }
}