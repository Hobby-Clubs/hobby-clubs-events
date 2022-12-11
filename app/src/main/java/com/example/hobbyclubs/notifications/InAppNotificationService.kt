package com.example.hobbyclubs.notifications

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.net.toUri
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.api.NotificationType.*
import com.example.hobbyclubs.general.toString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Service which fetches the [NotificationInfo] relevant to the current user
 * and creates notifications while the app is in the foreground or reduced
 *
 */
class InAppNotificationService : Service() {
    companion object {
        const val TAG = "InAppNotificationService"

        /**
         * Starts [InAppNotificationService]
         *
         * @param context
         * @param uid
         */
        fun start(context: Context, uid: String) {
            val intent =
                Intent(context, InAppNotificationService::class.java).apply { putExtra("uid", uid) }
            context.startService(intent)
        }

        /**
         * Stops [InAppNotificationService]
         *
         * @param context
         */
        fun stop(context: Context) {
            val intent = Intent(context, InAppNotificationService::class.java)
            context.stopService(intent)
        }

        /**
         * Checks whether [InAppNotificationService] is currently running
         *
         * @param context
         * @return true if it is running
         */
        fun isRunning(context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.runningAppProcesses.forEach {
                if (it.processName == InAppNotificationService::class.java.name) {
                    return true
                }
            }
            return false
        }

        const val NOTIF_UNREAD = "NOTIF_UNREAD"

        const val EXTRA_NOTIF_UNREAD = "EXTRA_NOTIF_UNREAD"
    }

    private val helper get() = InAppNotificationHelper(applicationContext)
    private val lastNotifPref
        get() = applicationContext.getSharedPreferences(
            "lastNotif", Context.MODE_PRIVATE
        )

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Starts a loop that fetches all relevant [NotificationInfo] according to an interval
     *
     * @param interval
     * @param uid
     * @param context
     */
    private fun startFetchLoop(interval: Long, uid: String, context: Context) {
        val isLoadingPref = applicationContext.getSharedPreferences("paused", MODE_PRIVATE)
        val isLoading = isLoadingPref.getBoolean("isPaused", false)
        if (!isLoading) {
            CoroutineScope(Dispatchers.IO).launch {
                fetchInAppNotifs()
                delay(interval)
                startFetchLoop(interval, uid, context)
            }
        }
    }

    /**
     * Fetches all the relevant [NotificationContent].
     * If it is the first time of the day that the user opens the app, a notification will indicate
     * the number of unread notifications. Otherwise, a notification will inform the user of any
     * new notification.
     * Also broadcasts a list of relevant [NotificationContent] so that they can be used in
     * HomeScreen (notification indicator) and NotificationScreen (list of notifications)
     *
     */
    private fun fetchInAppNotifs() {
        val lastNotifId = lastNotifPref.getString("lastNotifId", "")
        CoroutineScope(Dispatchers.IO).launch {
            val myNotifs = helper.getMyNotifs()
            if (myNotifs.isEmpty()) return@launch
            broadcastUnread(myNotifs)
            val today = Date().toString("dd.MM.YYYY")
            val isFirst = lastNotifPref.getString("lastDate", "") != today
            if (isFirst) {
                createFirstNotification(
                    myNotifs.filter { !it.readBy.contains(FirebaseHelper.uid) }.size
                )
                lastNotifPref.edit().apply {
                    putString("lastDate", today)
                    apply()
                }
                return@launch
            }
            if (lastNotifId != myNotifs.first().id) {
                lastNotifPref.edit().apply {
                    putString("lastNotifId", myNotifs.first().id)
                    apply()
                }
                createLatestNotification(myNotifs.first())
            }
        }
    }

    /**
     * Broadcasts a list of relevant [NotificationContent] so that they can be used in
     * HomeScreen (notification indicator) and NotificationScreen (list of notifications)
     *
     * @param unreads
     */
    private fun broadcastUnread(unreads: List<NotificationInfo>) {
        val isLoadingPref = applicationContext.getSharedPreferences("paused", MODE_PRIVATE)
        val isLoading = isLoadingPref.getBoolean("isPaused", false)
        if (!isLoading) {
            val intent = Intent()
            intent.action = NOTIF_UNREAD
            intent.putExtra(EXTRA_NOTIF_UNREAD, unreads.toTypedArray())
            sendBroadcast(intent)
        }
    }

    /**
     * Creates a notification for latest unread notification
     *
     * @param lastNotification
     */
    private fun createLatestNotification(lastNotification: NotificationInfo) {
        val type = valueOf(lastNotification.type)
        CoroutineScope(Dispatchers.IO).launch {
            val notifContent = when (type) {
                EVENT_CREATED -> helper.newEventToContent(lastNotification)
                NEWS_CLUB -> helper.clubNewsToContent(lastNotification)
                NEWS_GENERAL -> helper.generalNewsToContent(lastNotification)
                CLUB_REQUEST_PENDING -> helper.clubRequestToContent(lastNotification)
                CLUB_REQUEST_ACCEPTED -> helper.clubAcceptedRequestToContent(lastNotification)
                EVENT_REQUEST_PENDING -> helper.eventRequestToContent(lastNotification)
                EVENT_REQUEST_ACCEPTED -> helper.eventAcceptedRequestToContent(lastNotification)
            }
            notifContent?.let { content ->
                NotificationHelper.createNotification(applicationContext, content)
            }
        }
    }

    /**
     * Creates a notification for the first time of the day that the user opens the app.
     * The notification indicates how many notifications are left unread.
     *
     * @param amount
     */
    private fun createFirstNotification(amount: Int) {
        val deeplink = "https://hobbyclubs.fi/notif={all}".toUri()
        val data = NotificationContent(
            title = "Good to have you back!",
            content = "You have $amount unread notifications. Press here to see them",
            pendingIntent = NotificationHelper.getDeepLinkTapPendingIntent(
                applicationContext, deeplink, 89325795
            ),
            requestCode = 534927,
            channelId = "first"
        )
        NotificationHelper.createNotification(applicationContext, data)
    }

    /**
     * Starts the notification fetching loop when the service starts
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uid = intent?.getStringExtra("uid")

        uid?.let {
            startFetchLoop(10 * 1000, it, applicationContext)
        }
        return START_NOT_STICKY
    }
}