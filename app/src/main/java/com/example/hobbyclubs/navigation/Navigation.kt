package com.example.hobbyclubs.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for compose navigation
 */
sealed class NavRoutes(val route: String) {
    object LoginScreen: NavRoutes("LoginScreen")
    object HomeScreen: NavRoutes("HomeScreen")
    object ClubsScreen: NavRoutes("ClubsScreen")
    object NewsScreen: NavRoutes("NewsScreen")
    object CalendarScreen: NavRoutes("CalendarScreen")
    object EventScreen: NavRoutes("EventScreen")
    object EventParticipantsScreen: NavRoutes("EventParticipantsScreen")
    object EventManagementScreen: NavRoutes("EventManagementScreen")
    object ClubPageScreen: NavRoutes("ClubPageScreen")
    object ClubManagementScreen: NavRoutes("ClubManagementScreen")
    object ClubSettingsScreen: NavRoutes("ClubSettingsScreen")
    object ClubAllNewsScreen: NavRoutes("ClubAllNewsScreen")
    object ClubAllEventsScreen: NavRoutes("ClubAllEventsScreen")
    object ClubMembersScreen: NavRoutes("ClubMembersScreen")
    object ClubMemberRequestScreen: NavRoutes("ClubMemberRequestScreen")
    object CreateNewsScreen: NavRoutes("CreateNews")
    object CreateEvent: NavRoutes("CreateEvent")
    object CreateClub: NavRoutes("CreateClub")
    object SingleNewsScreen: NavRoutes("SingleNews")
    object FirstTimeScreen: NavRoutes("FirstTimeScreen")
}


data class BarItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)
object NavBarItems {
    val BarItems = listOf(

        BarItem(
            route = NavRoutes.HomeScreen.route,
            title = "Home",
            icon = Icons.Default.Home
        ),

        BarItem(
            route = NavRoutes.ClubsScreen.route,
            title = "Clubs",
            icon = Icons.Default.ClearAll
        ),

        BarItem(
            route = NavRoutes.CalendarScreen.route,
            title = "Events",
            icon = Icons.Default.EventNote
        )
    )
    object FirstTimeScreen: NavRoutes("FirstTimeScreen")
}