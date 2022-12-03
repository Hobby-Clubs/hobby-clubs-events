package com.example.hobbyclubs.notifications

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.net.toUri
import com.example.hobbyclubs.MainActivity
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.api.NotificationType.*
import com.example.hobbyclubs.general.toString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class InAppNotificationService : Service() {
    companion object {
        const val TAG = "InAppNotificationService"
        fun start(context: Context, uid: String) {
            val intent =
                Intent(context, InAppNotificationService::class.java).apply { putExtra("uid", uid) }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, InAppNotificationService::class.java)
            context.stopService(intent)
        }

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

    var isFirst = true
    val helper get() = InAppNotificationHelper(applicationContext)
    val lastNotifPref
        get() = applicationContext.getSharedPreferences(
            "lastNotif", Context.MODE_PRIVATE
        )

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun startFetchLoop(interval: Long, uid: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            generateInAppNotif()
            delay(interval)
            startFetchLoop(interval, uid, context)
        }
    }

    fun generateInAppNotif() {
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

    fun broadcastUnread(unreads: List<NotificationInfo>) {
        val intent = Intent()
        intent.action = NOTIF_UNREAD
        intent.putExtra(EXTRA_NOTIF_UNREAD, unreads.toTypedArray())
        sendBroadcast(intent)
        Log.d(TAG, "broadcastUnread: send ${unreads.size}")
    }

    fun createLatestNotification(lastNotification: NotificationInfo) {
        val type = valueOf(lastNotification.type)
        CoroutineScope(Dispatchers.IO).launch {
            val notifContent = when (type) {
                EVENT_CREATED -> helper.newEventToContent(lastNotification)
                NEWS_CLUB -> helper.clubNewsToContent(lastNotification)
                NEWS_GENERAL -> helper.generalNewsToContent(lastNotification)
                REQUEST_PENDING -> helper.requestToContent(lastNotification)
                REQUEST_ACCEPTED -> helper.acceptedRequestToContent(lastNotification)
            }
            notifContent?.let { content ->
                NotificationHelper.createNotification(applicationContext, content)
            }
        }
    }

    fun createFirstNotification(amount: Int) {
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uid = intent?.getStringExtra("uid")

        uid?.let {
            Log.d(TAG, "onStartCommand: service started")
            startFetchLoop(10 * 1000, it, applicationContext)
        }
        return START_NOT_STICKY
    }
}