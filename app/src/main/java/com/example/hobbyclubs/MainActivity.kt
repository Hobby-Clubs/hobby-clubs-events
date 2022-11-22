package com.example.hobbyclubs

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.compose.HobbyClubsTheme
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.calendar.CalendarScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubAllEventsScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubAllNewsScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubSettingsScreen
import com.example.hobbyclubs.screens.clubmembers.ClubMemberRequestScreen
import com.example.hobbyclubs.screens.clubmembers.ClubMembersScreen
import com.example.hobbyclubs.screens.clubpage.ClubPageScreen
import com.example.hobbyclubs.screens.clubs.ClubsScreen
import com.example.hobbyclubs.screens.create.club.CreateClubScreen
import com.example.hobbyclubs.screens.create.event.CreateEventScreen
import com.example.hobbyclubs.screens.createnews.CreateNewsScreen
import com.example.hobbyclubs.screens.event.EventScreen
import com.example.hobbyclubs.screens.firstTime.FirstTimeScreen
import com.example.hobbyclubs.screens.home.HomeScreen
import com.example.hobbyclubs.screens.login.LoginScreen
import com.example.hobbyclubs.screens.news.NewsScreen
import com.example.hobbyclubs.screens.news.SingleNewsScreen

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
        // EventScreen
        composable(
            NavRoutes.EventScreen.route + "/{id}",
            arguments = listOf(
                navArgument("id") {type = NavType.StringType}
            )
        ) {
            val id = it.arguments!!.getString("id")!!
            EventScreen(navController = navController, eventId = id)
        }
        // ClubPageScreen
        composable(
            NavRoutes.ClubPageScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://hobbyclubs.fi/{clubId}"
                    action = Intent.ACTION_VIEW
                }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubPageScreen(navController = navController, clubId = clubId)
        }
        // ClubManagementScreen
        composable(NavRoutes.ClubManagementScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubManagementScreen(navController = navController, clubId = clubId)
        }
        // ClubSettingsScreen
        composable(NavRoutes.ClubSettingsScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubSettingsScreen(navController = navController, clubId = clubId)
        }
        // ClubAllNewsScreen
        composable(NavRoutes.ClubAllNewsScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubAllNewsScreen(navController = navController, clubId = clubId)
        }
        // ClubAllEventsScreen
        composable(NavRoutes.ClubAllEventsScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubAllEventsScreen(navController = navController, clubId = clubId)
        }
        // ClubMembersScreen
        composable(
            NavRoutes.ClubMembersScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubMembersScreen(navController = navController, clubId = clubId)
        }
        // ClubMemberRequestScreen
        composable(
            NavRoutes.ClubMemberRequestScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            )
        ) {
            val clubId = it.arguments!!.getString("clubId")!!
            ClubMemberRequestScreen(navController = navController, clubId = clubId)
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

        // FirstTimeScreen
        composable(NavRoutes.FirstTimeScreen.route) {
            FirstTimeScreen(navController = navController)
        }
        // CreateNewsScreen
        composable(NavRoutes.CreateNewsScreen.route) {
            CreateNewsScreen(navController = navController)
        }
        // CreateEventScreen
        composable(NavRoutes.CreateEvent.route) {
            CreateEventScreen(navController = navController)
        }
        composable(
            NavRoutes.SingleNewsScreen.route + "/{newsId}",
            arguments = listOf(
                navArgument("newsId") {type = NavType.StringType}
            )

        ) {
            val newsId = it.arguments!!.getString("newsId")!!
            SingleNewsScreen(navController = navController, newsId = newsId)
        }
        // CreateClubScreen
        composable(NavRoutes.CreateClub.route) {
            CreateClubScreen(navController = navController)
        }
    }
}
