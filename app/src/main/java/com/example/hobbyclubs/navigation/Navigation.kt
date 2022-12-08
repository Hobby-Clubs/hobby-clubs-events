package com.example.hobbyclubs.navigation

import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import com.example.hobbyclubs.screens.*
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
import com.example.hobbyclubs.screens.login.LoginScreen
import com.example.hobbyclubs.screens.home.HomeScreen
import com.example.hobbyclubs.screens.news.NewsScreen
import com.example.hobbyclubs.screens.news.SingleNewsScreen
import com.example.hobbyclubs.screens.notifications.NotificationsScreen
import com.example.hobbyclubs.screens.settings.NotificationSettingsScreen

/**
 * Navigation routes for compose navigation
 */

val baseUrl = "https://hobbyclubs.fi/"

object NavHelper {
    const val baseUrl = "https://hobbyclubs.fi/"

    fun getDeepLinks(extensions: List<String>): List<NavDeepLink> {
        return extensions.map {
            navDeepLink {
                uriPattern = baseUrl + it
                action = Intent.ACTION_VIEW
            }
        }
    }
}

enum class NavRoute(
    val route: String,
    val args: List<NamedNavArgument> = listOf(),
    val deeplinks: List<NavDeepLink> = listOf(),
    val content: @Composable (NavBackStackEntry, NavController) -> Unit,
) {

    // Event-related

    Calendar(
        route = "Calendar",
        content = { _, navController ->
            CalendarScreen(navController = navController)
        }
    ),

    CreateEvent(
        route = "CreateEvent",
        content = { _, navController ->
            CreateEventScreen(navController = navController)
        }
    ),

    Event(
        route = "Event/{eventId}",
        args = listOf(navArgument("eventId") { type = NavType.StringType }),
        deeplinks = NavHelper.getDeepLinks(listOf("eventId={eventId}")),
        content = { entry, navController ->
            val eventId = entry.arguments?.getString("eventId")
            eventId?.let { id ->
                EventScreen(navController = navController, eventId = id)
            }
        }
    ),

    EventParticipants(
        route = "EventParticipants/{eventId}",
        args = listOf(navArgument("eventId") { type = NavType.StringType }),
        content = { entry, navController ->
            val eventId = entry.arguments?.getString("eventId")
            eventId?.let { id ->
                EventParticipantsScreen(navController = navController, eventId = id)
            }
        }
    ),

    EventParticipantRequest(
        route = "EventParticipantRequest/{eventId}",
        args = listOf(navArgument("eventId") { type = NavType.StringType }),
        deeplinks = NavHelper.getDeepLinks(listOf("requests/eventId={eventId}")),
        content = { entry, navController ->
            val eventId = entry.arguments?.getString("eventId")
            eventId?.let { id ->
                EventParticipantRequestScreen(navController = navController, eventId = id)
            }
        }
    ),
    EventManagement(
        route = "EventManagement/{eventId}",
        args = listOf(navArgument("eventId") { type = NavType.StringType }),
        content = { entry, navController ->
            val eventId = entry.arguments?.getString("eventId")
            eventId?.let { id ->
                EventManagementScreen(navController = navController, eventId = id)
            }
        }
    ),

    // Club-related

    Clubs(
        route = "Clubs",
        content = { _, navController ->
            ClubsScreen(navController = navController)
        }
    ),

    CreateClub(
        route = "CreateClub",
        content = { _, navController ->
            CreateClubScreen(navController = navController)
        }
    ),

    ClubPage(
        route = "ClubPage/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        deeplinks = NavHelper.getDeepLinks(listOf("clubId={clubId}")),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubPageScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubManagement(
        route = "ClubManagement/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubManagementScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubSettings(
        route = "ClubSettings/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubSettingsScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubAllNews(
        route = "ClubAllNews/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubAllNewsScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubAllEvents(
        route = "ClubAllEvents/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubAllEventsScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubMembers(
        route = "ClubMembers/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubMembersScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubMemberRequest(
        route = "ClubMemberRequest/{clubId}",
        args = listOf(navArgument("clubId") { type = NavType.StringType }),
        deeplinks = NavHelper.getDeepLinks(listOf("requests/clubId={clubId}")),
        content = { entry, navController ->
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubMemberRequestScreen(navController = navController, clubId = id)
            }
        }
    ),
    ClubNews(
        route = "ClubNews/{fromHome}/{clubId}",
        args = listOf(
            navArgument("fromHome") { type = NavType.BoolType },
            navArgument("clubId") { type = NavType.StringType }
        ),
        content = { entry, navController ->
            val fromHome = entry.arguments?.getBoolean("fromHome") ?: false
            val clubId = entry.arguments?.getString("clubId")
            clubId?.let { id ->
                ClubNewsScreen(
                    navController = navController,
                    clubId = id,
                    fromHomeScreen = fromHome
                )
            }
        }
    ),

    // News-related
    News(
        route = "News",
        content = { _, navController ->
            NewsScreen(navController = navController)
        }
    ),
    CreateNews(
        route = "CreateNews",
        content = { _, navController ->
            CreateNewsScreen(navController = navController)
        }
    ),
    SingleNews(
        route = "SingleNews/{newsId}",
        deeplinks = NavHelper.getDeepLinks(listOf("newsId={newsId}")),
        args = listOf(navArgument("newsId") { type = NavType.StringType }),
        content = { entry, navController ->
            val newsId = entry.arguments?.getString("newsId")
            newsId?.let { id ->
                SingleNewsScreen(navController = navController, newsId = id)
            }
        }
    ),

    // Other
    Login(
        route = "Login",
        content = { _, navController ->
            LoginScreen(navController = navController)
        }
    ),
    FirstTime(
        route = "FirstTime",
        content = { _, navController ->
            FirstTimeScreen(navController = navController)
        }),
    Home(
        route = "Home",
        content = { _, navController ->
            HomeScreen(navController = navController)
        }
    ),
    NotificationSettings(
        route = "NotificationSettings",
        content = { _, navController ->
            NotificationSettingsScreen(navController = navController)
        }
    ),
    Notifications(
        route = "Notifications",
        deeplinks = NavHelper.getDeepLinks(listOf("notif={all}")),
        content = { _, navController ->
            NotificationsScreen(navController = navController)
        }
    ),
    AllMy(
        route = "AllMy/{type}",
        args = listOf(navArgument("type") {type = NavType.StringType}),
        content = { entry, navController ->
            val type = entry.arguments?.getString("type")
            type?.let {
                AllMyScreen(
                    navController = navController,
                    type = it
                )
            }
        }
    ),
}

data class BarItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

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