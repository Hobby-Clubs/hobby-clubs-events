package com.example.hobbyclubs.notifications

import android.app.PendingIntent
import android.content.Context
import androidx.core.net.toUri
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.screens.settings.NotificationSetting
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Contains all the functions necessary to fetching the relevant notifications according to the
 * current user and their current notification settings
 *
 * @property context
 */
class InAppNotificationHelper(val context: Context) {

    /**
     * @return a list of [NotificationInfo] corresponding to newly added events by any club
     * the current user is a member of
     */
    private suspend fun getNewEventNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myClubIds =
                FirebaseHelper.getAllClubs().whereArrayContains("members", uid).get().await()
                    .toObjects(Club::class.java).map { it.ref }
            if (myClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.EVENT_CREATED.name)
                .whereIn("clubId", myClubIds).get().await().toObjects(NotificationInfo::class.java)
        }
        return listOf()
    }

    /**
     * @return a list of [NotificationInfo] corresponding to general news
     */
    private suspend fun getGeneralNewsNotifs(): List<NotificationInfo> {
        return FirebaseHelper.getNotifications()
            .whereEqualTo("type", NotificationType.NEWS_GENERAL.name).get().await()
            .toObjects(NotificationInfo::class.java)
    }

    /**
     * @return a list of [NotificationInfo] corresponding to the news of any club which the current user
     * is a member of
     */
    private suspend fun getClubNewsNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myClubIds =
                FirebaseHelper.getAllClubs().whereArrayContains("members", uid).get().await()
                    .toObjects(Club::class.java).map { it.ref }
            if (myClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.NEWS_CLUB.name).whereIn("clubId", myClubIds)
                .get().await().toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    /**
     * @return a list of [NotificationInfo] corresponding to membership requests made to any of the
     * clubs the current user administers
     */
    private suspend fun getClubRequestNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myAdminClubIds =
                FirebaseHelper.getAllClubs().whereArrayContains("admins", uid).get().await()
                    .toObjects(Club::class.java).map { it.ref }

            if (myAdminClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.CLUB_REQUEST_PENDING.name)
                .whereIn("clubId", myAdminClubIds).get().await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    /**
     * @return a list of [NotificationInfo] corresponding to the request to join clubs that
     * the current user made and which were accepted
     */
    private suspend fun getAcceptedClubRequestNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            return FirebaseHelper.getNotifications().whereEqualTo("userId", uid)
                .whereEqualTo("type", NotificationType.CLUB_REQUEST_ACCEPTED.name).get().await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    /**
     * @return a list of [NotificationInfo] corresponding to participation requests made to any of the
     * events the current user administers
     */
    private suspend fun getEventRequestNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myAdminEventIds =
                FirebaseHelper.getAllEvents().whereArrayContains("admins", uid).get().await()
                    .toObjects(Event::class.java).map { it.id }

            if (myAdminEventIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.EVENT_REQUEST_PENDING.name)
                .whereIn("eventId", myAdminEventIds).get().await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    /**
     * @return a list of [NotificationInfo] corresponding to the request to join events that
     * the current user made and which were accepted
     */
    private suspend fun getAcceptedEventRequestNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            return FirebaseHelper.getNotifications().whereEqualTo("userId", uid)
                .whereEqualTo("type", NotificationType.EVENT_REQUEST_ACCEPTED.name).get().await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    /**
     * @return a list of all the [NotificationInfo] relevant to the current user according to their
     * notification settings
     */
    suspend fun getMyNotifs(): List<NotificationInfo> {
        val mySettings = getNotificationSettings()
        val fetchedLists = mySettings.map { setting ->
            when (setting) {
                NotificationSetting.EVENT_NEW -> withContext(Dispatchers.IO) {
                    getNewEventNotifs()
                }
                NotificationSetting.NEWS_GENERAL -> withContext(Dispatchers.IO) {
                    getGeneralNewsNotifs()
                }
                NotificationSetting.NEWS_CLUB -> withContext(Dispatchers.IO) {
                    getClubNewsNotifs()
                }
                NotificationSetting.REQUEST_MEMBERSHIP -> withContext(Dispatchers.IO) {
                    getClubRequestNotifs()
                }
                NotificationSetting.REQUEST_MEMBERSHIP_ACCEPTED -> withContext(Dispatchers.IO) {
                    getAcceptedClubRequestNotifs()
                }
                NotificationSetting.REQUEST_PARTICIPATION -> withContext(Dispatchers.IO) {
                    getEventRequestNotifs()
                }
                NotificationSetting.REQUEST_PARTICIPATION_ACCEPTED -> withContext(Dispatchers.IO) {
                    getAcceptedEventRequestNotifs()
                }
                NotificationSetting.EVENT_HOUR_REMINDER,
                NotificationSetting.EVENT_DAY_REMINDER -> listOf()
            }
        }
        return fetchedLists.reduceOrNull { it1, it2 -> it1 + it2 }?.sortedByDescending { it.time }
            ?: listOf()
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for a new event created
     *
     * @param notification
     * @return the content of notification for a new event created
     */
    suspend fun newEventToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId).get().await()
                .toObject(Club::class.java)?.name
        }
        val event = withContext(Dispatchers.IO) {
            FirebaseHelper.getEvent(notification.eventId).get().await().toObject(Event::class.java)
        }

        event?.let { ev ->
            if (ev.date < Timestamp.now()) {
                return null
            }
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(ev.date.toDate())
            val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(ev.date.toDate())
            val title = "New event hosted by $clubName"
            val content = " ${ev.name} / $date at $time"
            return NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 54395,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.EVENT_NEW,
                navRoute = "${NavRoute.Event.name}/${ev.id}"
            )
        } ?: return null
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for a general news
     *
     * @param notification
     * @return the content of notification for a general news
     */
    suspend fun generalNewsToContent(notification: NotificationInfo): NotificationContent? {
        val newsArticle =
            FirebaseHelper.getNews(notification.newsId).get().await().toObject(News::class.java)

        return newsArticle?.let { news ->
            val title = "General announcement"
            val content = news.headline
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 9523874,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.NEWS_GENERAL,
                navRoute = "${NavRoute.SingleNews.name}/${notification.newsId}"
            )
        }
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for club news
     *
     * @param notification
     * @return the content of notification for a club news
     */
    suspend fun clubNewsToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId).get().await()
                .toObject(Club::class.java)?.name
        }
        val newsArticle = withContext(Dispatchers.IO) {
            FirebaseHelper.getNews(notification.newsId).get().await().toObject(News::class.java)
        }

        return newsArticle?.let { news ->
            val title = "News from $clubName"
            val content = news.headline
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 25345,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.NEWS_CLUB,
                navRoute = "${NavRoute.SingleNews.name}/${newsArticle.id}"
            )
        }
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for a club membership request
     *
     * @param notification
     * @return the content of notification for a club membership request
     */
    suspend fun clubRequestToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId).get().await()
                .toObject(Club::class.java)?.name
        }
        val user = withContext(Dispatchers.IO) {
            FirebaseHelper.getUser(notification.userId).get().await().toObject(User::class.java)
        }

        return user?.let { u ->
            val title = "Membership request"
            val content = "${u.fName} ${u.lName} has requested to join $clubName"
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 928375,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.REQUEST_PARTICIPATION,
                navRoute = "${NavRoute.ClubMemberRequest.name}/${notification.clubId}"
            )
        }
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for an accepted club membership request
     *
     * @param notification
     * @return the content of notification for an accepted club membership request
     */
    suspend fun clubAcceptedRequestToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId).get().await()
                .toObject(Club::class.java)?.name
        }

        return clubName?.let { name ->
            val title = "Welcome to $name"
            val content = "Your request to join the club was accepted"
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 5324,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.REQUEST_MEMBERSHIP_ACCEPTED,
                navRoute = "${NavRoute.ClubPage.name}/${notification.clubId}"
            )
        }
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for an event participation request
     *
     * @param notification
     * @return the content of notification for an event participation request
     */
    suspend fun eventRequestToContent(notification: NotificationInfo): NotificationContent? {
        val eventName = withContext(Dispatchers.IO) {
            FirebaseHelper.getEvent(notification.eventId).get().await()
                .toObject(Event::class.java)?.name
        }
        val user = withContext(Dispatchers.IO) {
            FirebaseHelper.getUser(notification.userId).get().await().toObject(User::class.java)
        }

        return user?.let { u ->
            val title = "Event participation request"
            val content =
                "${u.fName} ${u.lName} has requested to join the following event: $eventName"
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 98543,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.REQUEST_PARTICIPATION,
                navRoute = "${NavRoute.EventParticipantRequest.name}/${notification.eventId}"
            )
        }
    }

    /**
     * Converts a [NotificationInfo] into a [NotificationContent] which contains all the data
     * needed to create a notification for an accepted event participation request
     *
     * @param notification
     * @return the content of notification for an accepted event participation request
     */
    suspend fun eventAcceptedRequestToContent(notification: NotificationInfo): NotificationContent {
        val eventName = withContext(Dispatchers.IO) {
            FirebaseHelper.getEvent(notification.eventId).get().await()
                .toObject(Club::class.java)?.name
        }

        val title = "Welcome to $eventName"
        val content = "Your request to join the club was accepted"

        return NotificationContent(
            id = notification.id,
            title = title,
            content = content,
            pendingIntent = getTapPendingIntent(context, notification),
            requestCode = 17289,
            channelId = notification.type,
            date = notification.time.toDate(),
            setting = NotificationSetting.REQUEST_PARTICIPATION_ACCEPTED,
            navRoute = "${NavRoute.Event.name}/${notification.eventId}"
        )
    }

    /**
     * @return a list of all relevant [NotificationContent] which correspond to notifications the current
     * user hasn't read yet
     */
    suspend fun getMyNotifsContents(): List<NotificationContent> {
        val mySettings = getNotificationSettings()
        val fetchedLists = mySettings.map { setting ->
            when (setting) {
                NotificationSetting.EVENT_NEW -> withContext(Dispatchers.IO) {
                    getNewEventNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            newEventToContent(it)
                        }
                }
                NotificationSetting.NEWS_GENERAL -> withContext(Dispatchers.IO) {
                    getGeneralNewsNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            generalNewsToContent(it)
                        }
                }
                NotificationSetting.NEWS_CLUB -> withContext(Dispatchers.IO) {
                    getClubNewsNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            clubNewsToContent(it)
                        }
                }
                NotificationSetting.REQUEST_PARTICIPATION -> withContext(Dispatchers.IO) {
                    getClubRequestNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            clubRequestToContent(it)
                        }
                }
                NotificationSetting.REQUEST_MEMBERSHIP_ACCEPTED -> withContext(Dispatchers.IO) {
                    getAcceptedClubRequestNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            clubAcceptedRequestToContent(it)
                        }
                }
                NotificationSetting.REQUEST_MEMBERSHIP -> withContext(Dispatchers.IO) {
                    getEventRequestNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            eventRequestToContent(it)
                        }
                }
                NotificationSetting.REQUEST_PARTICIPATION_ACCEPTED -> withContext(Dispatchers.IO) {
                    getAcceptedEventRequestNotifs().filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .map {
                            eventAcceptedRequestToContent(it)
                        }
                }
                NotificationSetting.EVENT_HOUR_REMINDER, NotificationSetting.EVENT_DAY_REMINDER -> listOf()
            }
        }

        return fetchedLists.reduceOrNull { it1, it2 -> it1 + it2 }?.sortedByDescending { it.date }
            ?: listOf()
    }

    /**
     * Returns the pending intent which will redirect the user to the relevant screen when they
     * tap a notification (e.g. New event notification -> EventScreen of the event)
     *
     * @param context
     * @param notification
     * @return a pending intent to navigate to the relevant screen when a notification is tapped
     */
    private fun getTapPendingIntent(context: Context, notification: NotificationInfo): PendingIntent {
        val type = NotificationType.valueOf(notification.type)
        val baseUrl = "https://hobbyclubs.fi/"
        val ending = when (type) {
            NotificationType.EVENT_CREATED -> "eventId=${notification.eventId}"
            NotificationType.NEWS_CLUB -> "newsId=${notification.newsId}"
            NotificationType.NEWS_GENERAL -> "newsId=${notification.newsId}"
            NotificationType.CLUB_REQUEST_PENDING -> "requests/clubId=${notification.clubId}"
            NotificationType.CLUB_REQUEST_ACCEPTED -> "clubId=${notification.clubId}"
            NotificationType.EVENT_REQUEST_PENDING -> "requests/eventId=${notification.eventId}"
            NotificationType.EVENT_REQUEST_ACCEPTED -> "eventId=${notification.eventId}"
        }
        return NotificationHelper.getDeepLinkTapPendingIntent(
            context = context, deepLink = (baseUrl + ending).toUri(), requestCode = 890797
        )
    }

    /**
     * @return a list of the notification settings which are currently enabled
     */
    fun getNotificationSettings(): List<NotificationSetting> {
        return NotificationSetting.values()
            .map { setting -> setting.apply { isActive = getBoolVal(setting.name) } }
            .filter { it.isActive }
    }

    /**
     * Returns a boolean which corresponds to the state of a notification setting in shared preferences
     *
     * @param key
     */
    private fun getBoolVal(key: String) =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean(key, false)

}
