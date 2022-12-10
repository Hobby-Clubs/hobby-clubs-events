package com.example.hobbyclubs.navigation

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar
 *
 * @param navController
 */
@Composable
fun BottomBar(navController: NavController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        NavBarItems.BarItems.forEach { barItem ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == barItem.route } == true,
                onClick = {
                    navController.navigate(barItem.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = barItem.icon,
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant
                    )
                },
                label = { Text(text = barItem.title) }
            )
        }
    }
}

/**
 * Represents an item of the BottomNavigationBar
 *
 * @property title
 * @property icon
 * @property route
 */
data class BarItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

/**
 * Nav bar items
 *
 */
object NavBarItems {
    val BarItems = listOf(
        BarItem(
            route = NavRoute.Home.route,
            title = "Home",
            icon = Icons.Outlined.Home
        ),
        BarItem(
            route = NavRoute.Calendar.route,
            title = "Events",
            icon = Icons.Outlined.CalendarMonth
        ),
        BarItem(
            route = NavRoute.Clubs.route,
            title = "Clubs",
            icon = Icons.Outlined.ClearAll
        )
    )
}
