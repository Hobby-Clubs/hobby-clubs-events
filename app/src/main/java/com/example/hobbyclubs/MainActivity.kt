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
import com.example.hobbyclubs.screens.eventmanagement.EventManagementScreen
import com.example.hobbyclubs.database.EventAlarmDBHelper
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.notifications.AlarmReceiver
import com.example.hobbyclubs.screens.allmyoftype.AllMyScreen
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
import com.example.hobbyclubs.screens.create.news.CreateNewsScreen
import com.example.hobbyclubs.screens.event.EventScreen
import com.example.hobbyclubs.screens.eventparticipants.EventParticipantRequestScreen
import com.example.hobbyclubs.screens.eventparticipants.EventParticipantsScreen
import com.example.hobbyclubs.screens.firstTime.FirstTimeScreen
import com.example.hobbyclubs.screens.home.HomeScreen
import com.example.hobbyclubs.screens.login.LoginScreen
import com.example.hobbyclubs.screens.news.NewsScreen
import com.example.hobbyclubs.screens.news.SingleNewsScreen
import com.example.hobbyclubs.screens.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlarmReceiver.createNotificationChannel(this)
        val eventAlarmDBHelper = EventAlarmDBHelper(this)
        eventAlarmDBHelper.updateAlarms()
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
        // AllMyScreen - All my clubs/events/news
        composable(
            NavRoutes.AllMyScreen.route + "/{type}",
            arguments = listOf(navArgument("type") {type = NavType.StringType}),
        ) {
            val type = it.arguments!!.getString("type")!!
            AllMyScreen(navController = navController, type = type)
        }
        // EventScreen
        composable(
            NavRoutes.EventScreen.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") {type = NavType.StringType}),
            deepLinks = listOf(navDeepLink {
                uriPattern = "https://hobbyclubs.fi/eventId={eventId}"
                action = Intent.ACTION_VIEW
            })
        ) {
            val id = it.arguments!!.getString("eventId")!!
            EventScreen(navController = navController, eventId = id)
        }
        // EventParticipantsScreen
        composable(
            NavRoutes.EventParticipantsScreen.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) {
            val eventId = it.arguments!!.getString("eventId")!!
            EventParticipantsScreen(navController = navController, eventId = eventId)
        }
        // EventParticipantRequestScreen
        composable(
            NavRoutes.EventParticipantRequestScreen.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) {
            val eventId = it.arguments!!.getString("eventId")!!
            EventParticipantRequestScreen(navController = navController, eventId = eventId)
        }
        // EventManagementScreen
        composable(
            NavRoutes.EventManagementScreen.route + "/{eventId}",
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType }
            )
        ) {
            val eventId = it.arguments!!.getString("eventId")!!
            EventManagementScreen(navController = navController, eventId = eventId)
        }
        // ClubPageScreen
        composable(
            NavRoutes.ClubPageScreen.route + "/{clubId}",
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "https://hobbyclubs.fi/clubId={clubId}"
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
        // SettingsScreen
        composable(NavRoutes.SettingsScreen.route) {
            SettingsScreen(navController = navController)
        }
    }
}
