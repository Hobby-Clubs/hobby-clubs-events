package com.example.hobbyclubs.navigation

/**
 * Navigation routes for compose navigation
 */
sealed class NavRoutes(val route: String) {
    object LoginScreen: NavRoutes("LoginScreen")
    object HomeScreen: NavRoutes("HomeScreen")
    object ClubsScreen: NavRoutes("ClubsScreen")
    object NewsScreen: NavRoutes("NewsScreen")
    object CalendarScreen: NavRoutes("CalendarScreen")
    object ClubPageScreen: NavRoutes("ClubPageScreen")
    object ClubManagementScreen: NavRoutes("ClubManagementScreen")
    object MembersScreen: NavRoutes("MembersScreen")
    object FirstTimeScreen: NavRoutes("FirstTimeScreen")
}