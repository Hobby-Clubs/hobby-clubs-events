package com.example.hobbyclubs

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.HobbyClubsTheme
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.calendar.CalendarScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementScreen
import com.example.hobbyclubs.screens.clubpage.ClubPageScreen
import com.example.hobbyclubs.screens.clubs.ClubsScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementScreen
import com.example.hobbyclubs.screens.clubpage.ClubPageScreen
import com.example.hobbyclubs.screens.clubs.ClubsScreen
import com.example.hobbyclubs.screens.createnews.CreateNewsScreen
import com.example.hobbyclubs.screens.home.HomeScreen
import com.example.hobbyclubs.screens.login.LoginScreen
import com.example.hobbyclubs.screens.members.ClubMembersScreen
import com.example.hobbyclubs.screens.news.NewsScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HobbyClubsTheme {
                MyAppNavHost()
            }
        }
    }
}

// Jetpack Compose navigation host
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MyAppNavHost() {

    val navController = rememberNavController()
    val startDestination =
        if (FirebaseHelper.currentUser == null) {
            NavRoutes.LoginScreen.route
        } else {
            NavRoutes.HomeScreen.route
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login
        composable(NavRoutes.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        // HomeScreen
        composable(NavRoutes.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        // ClubPageScreen
        composable(NavRoutes.ClubPageScreen.route) {
            ClubPageScreen(navController = navController)
        }
        // ClubManagementScreen
        composable(NavRoutes.ClubManagementScreen.route) {
            ClubManagementScreen(navController = navController)
        }
        // ClubMembersScreen
        composable(NavRoutes.MembersScreen.route) {
            ClubMembersScreen(navController = navController)
        }
        // NewsScreen
        composable(NavRoutes.NewsScreen.route) {
            NewsScreen(navController = navController)
        }
        // CalendarScreen
        composable(NavRoutes.CalendarScreen.route) {
            CalendarScreen(navController = navController)
        }
        // ClubsScreen
        composable(NavRoutes.ClubsScreen.route) {
            ClubsScreen(navController = navController)
        }
        // CreateNewsScreen
        composable(NavRoutes.CreateNewsScreen.route) {
            CreateNewsScreen(navController = navController)
        }
    }
}
