package com.example.hobbyclubs.notifications

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.settings.NotificationSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class InAppNotificationHelper(val context: Context) {
    fun getMyNewEvents(
        notifications: List<NotificationInfo>,
        myClubs: List<String>
    ): List<NotificationInfo> {
        return notifications
            .filter { it.type == NotificationType.EVENT_CREATED.name }
            .filter { myClubs.contains(it.clubId) }
    }

    fun getGeneralNews(notifications: List<NotificationInfo>): List<NotificationInfo> {
        return notifications
            .filter { it.type == NotificationType.NEWS_GENERAL.name }
    }

    fun getMyClubNews(
        notifications: List<NotificationInfo>,
        myClubs: List<String>
    ): List<NotificationInfo> {
        return notifications
            .filter { it.type == NotificationType.NEWS_CLUB.name }
            .filter { myClubs.contains(it.clubId) }
    }

    fun getMemberRequests(
        notifications: List<NotificationInfo>,
        myAdminClubs: List<String>
    )
            : List<NotificationInfo> {
        return notifications
            .filter { it.type == NotificationType.REQUEST_PENDING.name }
            .filter { myAdminClubs.contains(it.clubId) }
    }

    fun getAcceptedRequests(
        notifications: List<NotificationInfo>,
        uid: String
    ): List<NotificationInfo> {
        return notifications
            .filter { it.type == NotificationType.REQUEST_ACCEPTED.name }
            .filter { it.userId == uid }
    }

    fun myNotificationsFilter(
        allNotifications: List<NotificationInfo>,
        myClubs: List<Club>,
        uid: String
    ): List<NotificationInfo> {
        val activeSettings = getNotificationSettings()
        val myNotifications = mutableListOf<NotificationInfo>()
        val myClubIds = myClubs.map { it.ref }
        val myAdminClubIds = myClubs
            .filter { club -> club.admins.contains(uid) }
            .map { club -> club.ref }
        activeSettings.forEach { setting ->
            myNotifications.addAll(
                when (setting) {
                    NotificationSetting.EVENT_NEW -> getMyNewEvents(allNotifications, myClubIds)
                    NotificationSetting.NEWS_GENERAL -> getGeneralNews(allNotifications)
                    NotificationSetting.NEWS_CLUB -> getMyClubNews(allNotifications, myClubIds)
                    NotificationSetting.REQUEST_MEMBERSHIP -> getMemberRequests(
                        allNotifications,
                        myAdminClubIds
                    )
                    NotificationSetting.REQUEST_ACCEPTED -> getAcceptedRequests(
                        allNotifications,
                        uid
                    )
                    NotificationSetting.EVENT_HOUR_REMINDER,
                    NotificationSetting.EVENT_DAY_REMINDER -> listOf()
                }
            )
        }
        return myNotifications.sortedByDescending { it.time }
    }

    suspend fun getNewEventNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myClubIds = FirebaseHelper.getAllClubs()
                .whereArrayContains("members", uid)
                .get()
                .await()
                .toObjects(Club::class.java)
                .map { it.ref }
            if (myClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.EVENT_CREATED.name)
                .whereIn("clubId", myClubIds)
                .get()
                .await()
                .toObjects(NotificationInfo::class.java)
        }
        return listOf()
    }

    suspend fun getGeneralNewsNotifs(): List<NotificationInfo> {
        return FirebaseHelper.getNotifications()
            .whereEqualTo("type", NotificationType.NEWS_GENERAL.name)
            .get()
            .await()
            .toObjects(NotificationInfo::class.java)
    }

    suspend fun getClubNewsNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myClubIds = FirebaseHelper.getAllClubs()
                .whereArrayContains("members", uid)
                .get()
                .await()
                .toObjects(Club::class.java)
                .map { it.ref }
            if (myClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.NEWS_CLUB.name)
                .whereIn("clubId", myClubIds)
                .get()
                .await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    suspend fun getRequestNotif(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            val myAdminClubIds = FirebaseHelper.getAllClubs()
                .whereArrayContains("admins", uid)
                .get()
                .await()
                .toObjects(Club::class.java)
                .map { it.ref }

            if (myAdminClubIds.isEmpty()) return listOf()

            return FirebaseHelper.getNotifications()
                .whereEqualTo("type", NotificationType.REQUEST_PENDING.name)
                .whereIn("clubId", myAdminClubIds)
                .get()
                .await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()
    }

    suspend fun getAcceptedRequestNotifs(): List<NotificationInfo> {
        FirebaseHelper.uid?.let { uid ->
            return FirebaseHelper.getNotifications()
                .whereEqualTo("userId", uid)
                .whereEqualTo("type", NotificationType.REQUEST_ACCEPTED.name)
                .get()
                .await()
                .toObjects(NotificationInfo::class.java)
        } ?: return listOf()

    }

    suspend fun getMyNotifs(): List<NotificationInfo> {
        val mySettings = getNotificationSettings()
        Log.d("notificationTest", mySettings.map { it.name }.toString())
        val fetchedLists = mySettings.map { setting ->
            when (setting) {
                NotificationSetting.EVENT_NEW -> withContext(Dispatchers.IO) { getNewEventNotifs() }
                NotificationSetting.NEWS_GENERAL -> withContext(Dispatchers.IO) { getGeneralNewsNotifs() }
                NotificationSetting.NEWS_CLUB -> withContext(Dispatchers.IO) { getClubNewsNotifs() }
                NotificationSetting.REQUEST_MEMBERSHIP -> withContext(Dispatchers.IO) { getRequestNotif() }
                NotificationSetting.REQUEST_ACCEPTED -> withContext(Dispatchers.IO) { getAcceptedRequestNotifs() }
                else -> withContext(Dispatchers.IO) { listOf() }
            }
        }
        return fetchedLists
            .reduceOrNull { it1, it2 -> it1 + it2 }
            ?.sortedByDescending { it.time }
            ?: listOf()
    }

    suspend fun newEventToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId)
                .get()
                .await()
                .toObject(Club::class.java)?.name
        }
        val event = withContext(Dispatchers.IO) {
            FirebaseHelper.getEvent(notification.eventId)
                .get()
                .await()
                .toObject(Event::class.java)
        }
        return event?.let { ev ->
            val date = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(ev.date.toDate())
            val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(ev.date.toDate())
            val title = "New event hosted by $clubName"
            val content = " ${ev.name} / $date at $time"
            NotificationContent(
                id = notification.id,
                title = title,
                content = content,
                pendingIntent = getTapPendingIntent(context, notification),
                requestCode = 54395,
                channelId = notification.type,
                date = notification.time.toDate(),
                setting = NotificationSetting.EVENT_NEW,
                navRoute = "${NavRoutes.EventScreen.route}/${ev.id}"
            )
        }
    }

    suspend fun generalNewsToContent(notification: NotificationInfo): NotificationContent? {
        val newsArticle = FirebaseHelper.getNews(notification.newsId)
            .get()
            .await()
            .toObject(News::class.java)

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
                navRoute = "${NavRoutes.SingleNewsScreen.route}/${notification.newsId}"
            )
        }
    }

    suspend fun clubNewsToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId)
                .get()
                .await()
                .toObject(Club::class.java)?.name
        }
        val newsArticle = withContext(Dispatchers.IO) {
            FirebaseHelper.getNews(notification.newsId)
                .get()
                .await()
                .toObject(News::class.java)
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
                navRoute = "${NavRoutes.SingleNewsScreen.route}/${newsArticle.id}"
            )
        }
    }

    suspend fun requestToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId)
                .get()
                .await()
                .toObject(Club::class.java)?.name
        }
        val user = withContext(Dispatchers.IO) {
            FirebaseHelper.getUser(notification.userId)
                .get()
                .await()
                .toObject(User::class.java)
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
                setting = NotificationSetting.REQUEST_MEMBERSHIP,
                navRoute = "${NavRoutes.ClubMemberRequestScreen.route}/${notification.clubId}"
            )
        }
    }

    suspend fun acceptedRequestToContent(notification: NotificationInfo): NotificationContent? {
        val clubName = withContext(Dispatchers.IO) {
            FirebaseHelper.getClub(notification.clubId)
                .get()
                .await()
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
                setting = NotificationSetting.REQUEST_ACCEPTED,
                navRoute = "${NavRoutes.ClubPageScreen.route}/${notification.clubId}"
            )
        }
    }

    suspend fun getMyNotifsContents(): List<NotificationContent> {
        val mySettings = getNotificationSettings()
        val fetchedLists = mySettings.map { setting ->
            when (setting) {
                NotificationSetting.EVENT_NEW -> withContext(Dispatchers.IO) {
                    getNewEventNotifs()
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            newEventToContent(it)
                        }
                }
                NotificationSetting.NEWS_GENERAL -> withContext(Dispatchers.IO) {
                    getGeneralNewsNotifs()
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            generalNewsToContent(it)
                        }
                }
                NotificationSetting.NEWS_CLUB -> withContext(Dispatchers.IO) {
                    getClubNewsNotifs()
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            clubNewsToContent(it)
                        }
                }
                NotificationSetting.REQUEST_MEMBERSHIP -> withContext(Dispatchers.IO) {
                    getRequestNotif()
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            requestToContent(it)
                        }
                }
                NotificationSetting.REQUEST_ACCEPTED -> withContext(Dispatchers.IO) {
                    getAcceptedRequestNotifs()
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                        .mapNotNull {
                            acceptedRequestToContent(it)
                        }
                }
                else -> withContext(Dispatchers.IO) { listOf() }
            }
        }

        return fetchedLists.reduceOrNull { it1, it2 -> it1 + it2 }
            ?.sortedByDescending { it.date }
            ?: listOf()
    }

    fun getTapPendingIntent(context: Context, notification: NotificationInfo): PendingIntent {
        val type = NotificationType.valueOf(notification.type)
        val baseUrl = "https://hobbyclubs.fi/"
        val ending = when (type) {
            NotificationType.EVENT_CREATED -> "eventId=${notification.eventId}"
            NotificationType.NEWS_CLUB -> "newsId=${notification.newsId}"
            NotificationType.NEWS_GENERAL -> "newsId=${notification.newsId}"
            NotificationType.REQUEST_PENDING -> "requests/clubId=${notification.clubId}"
            NotificationType.REQUEST_ACCEPTED -> "clubId=${notification.clubId}"
        }
        Log.d("pendingIntent", "${notification.type}: $baseUrl$ending")
        return NotificationHelper.getDeepLinkTapPendingIntent(
            context = context,
            deepLink = (baseUrl + ending).toUri(),
            requestCode = 890797
        )
    }

    fun getNotificationSettings(): List<NotificationSetting> {
        return NotificationSetting.values()
            .map { setting -> setting.apply { isActive = getBoolVal(setting.name) } }
            .filter { it.isActive }
    }

    fun getBoolVal(key: String) = context
        .getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getBoolean(key, false)

}
