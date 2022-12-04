package com.example.hobbyclubs

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.HobbyClubsTheme
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.NotificationType
import com.example.hobbyclubs.database.EventAlarmDBHelper
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.notifications.*
import com.example.hobbyclubs.screens.allmyoftype.AllMyScreen
import com.example.hobbyclubs.screens.calendar.CalendarScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubAllEventsScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubAllNewsScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementScreen
import com.example.hobbyclubs.screens.clubmanagement.ClubSettingsScreen
import com.example.hobbyclubs.screens.clubmembers.ClubMemberRequestScreen
import com.example.hobbyclubs.screens.clubmembers.ClubMembersScreen
import com.example.hobbyclubs.screens.clubpage.ClubNewsScreen
import com.example.hobbyclubs.screens.clubpage.ClubPageScreen
import com.example.hobbyclubs.screens.clubs.ClubsScreen
import com.example.hobbyclubs.screens.create.club.CreateClubScreen
import com.example.hobbyclubs.screens.create.event.CreateEventScreen
import com.example.hobbyclubs.screens.create.news.CreateNewsScreen
import com.example.hobbyclubs.screens.event.EventScreen
import com.example.hobbyclubs.screens.eventmanagement.EventManagementScreen
import com.example.hobbyclubs.screens.eventparticipants.EventParticipantRequestScreen
import com.example.hobbyclubs.screens.eventparticipants.EventParticipantsScreen
import com.example.hobbyclubs.screens.firstTime.FirstTimeScreen
import com.example.hobbyclubs.screens.home.HomeScreen
import com.example.hobbyclubs.screens.login.LoginScreen
import com.example.hobbyclubs.screens.news.NewsScreen
import com.example.hobbyclubs.screens.news.SingleNewsScreen
import com.example.hobbyclubs.screens.notifications.NotificationScreen
import com.example.hobbyclubs.screens.settings.NotificationSetting
import com.example.hobbyclubs.screens.settings.NotificationSettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberNavController()

            HobbyClubsTheme {
                MyAppNavHost(navController as NavHostController)
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            setupEventRemindersChannel()
            setupInAppNotificationChannels()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navController.handleDeepLink(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        InAppNotificationService.stop(this)
    }

    fun setupInAppNotificationChannels() {
        NotificationHelper.createNotificationChannel(
            this,
            NotificationChannelData(
                id = "first",
                name = "Launch notification",
                description = "Notifications for the notification on launch"
            )
        )
        NotificationType.values()
            .forEach { type ->
                NotificationHelper.createNotificationChannel(
                    this,
                    NotificationChannelData(
                        id = type.name,
                        name = type.channelName,
                        description = "Notifications for ${type.channelName}"
                    )
                )
            }
    }

    fun setupEventRemindersChannel() {
        val settings = InAppNotificationHelper(this).getNotificationSettings()
        val hasHourReminder = settings.contains(NotificationSetting.EVENT_HOUR_REMINDER)
        val hasDayReminder = settings.contains(NotificationSetting.EVENT_DAY_REMINDER)
        val eventAlarmDBHelper = EventAlarmDBHelper(this)
        if (hasHourReminder || hasDayReminder) {
            AlarmReceiver.createNotificationChannel(this)
        }
        eventAlarmDBHelper.updateAlarms()
    }
}

// Jetpack Compose navigation host
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MyAppNavHost(navController: NavHostController) {
    val startDestination =
        if (FirebaseHelper.currentUser == null) {
            NavRoutes.LoginScreen.route
        } else {
            NavRoutes.HomeScreen.route
        }

    val baseUrl = "https://hobbyclubs.fi/"
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
            arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink {
                uriPattern = baseUrl + "eventId={eventId}"
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
                    uriPattern = baseUrl + "clubId={clubId}"
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
        // ClubNewsScreen
        composable(NavRoutes.ClubNewsScreen.route + "/{fromHome}/{clubId}",
            arguments = listOf(
                navArgument("fromHome") { type = NavType.BoolType },
                navArgument("clubId") { type = NavType.StringType }

            )
        ) {
            val fromHome = it.arguments!!.getBoolean("fromHome")
            val clubId = it.arguments!!.getString("clubId")!!
            ClubNewsScreen(navController = navController, clubId = clubId, fromHomeScreen = fromHome)
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
            deepLinks = listOf(navDeepLink {
                uriPattern = baseUrl + "requests/clubId={clubId}"
                action = Intent.ACTION_VIEW
            }),
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
            deepLinks = listOf(navDeepLink {
                uriPattern = baseUrl + "newsId={newsId}"
                action = Intent.ACTION_VIEW
            }),
            arguments = listOf(
                navArgument("newsId") { type = NavType.StringType }
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
            NotificationSettingsScreen(navController = navController)
        }
        // NotificationScreen
        composable(
            NavRoutes.NotificationScreen.route,
            deepLinks = listOf(navDeepLink {
                uriPattern = baseUrl + "notif={all}"
                action = Intent.ACTION_VIEW
            })
        ) {
            NotificationScreen(navController = navController)
        }
    }
}
