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
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.notifications.*
import com.example.hobbyclubs.screens.settings.NotificationSetting
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
            NavRoute.Login.route
        } else {
            NavRoute.Home.route
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        NavRoute.values().forEach { navRoute ->
            composable(
                route = navRoute.route,
                arguments = navRoute.args,
                deepLinks = navRoute.deeplinks,
                content = {
                    navRoute.content(it, navController)
                }
            )
        }
    }
}
