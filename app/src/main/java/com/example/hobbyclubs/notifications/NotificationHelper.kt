package com.example.hobbyclubs.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hobbyclubs.MainActivity
import com.example.hobbyclubs.R
import com.example.hobbyclubs.screens.settings.NotificationSetting
import java.util.*

/**
 * Contains the functions needed to create notification channels as well as notifications and their
 * pending intent
 *
 */
object NotificationHelper {
    /**
     * Creates a notification channel
     *
     * @param context
     * @param data
     */
    fun createNotificationChannel(context: Context, data: NotificationChannelData) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(
            data.id,
            data.name,
            importance
        ).apply {
            description = data.description
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Creates a notification
     *
     * @param context
     * @param data
     */
    fun createNotification(context: Context, data: NotificationContent) {
        val notification =
            NotificationCompat.Builder(context, data.channelId)
                .setContentTitle(data.title)
                .setContentText(data.content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(data.pendingIntent)
                .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(data.requestCode, notification)
    }

    /**
     * Returns a pending intent which redirects to the relevant screen the user
     * when they tap a notification
     *
     * @param context
     * @param deepLink
     * @param requestCode
     * @return
     */
    fun getDeepLinkTapPendingIntent(
        context: Context,
        deepLink: Uri,
        requestCode: Int
    ): PendingIntent {
        val taskDetailIntent = Intent(
            Intent.ACTION_VIEW,
            deepLink,
            context,
            MainActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(taskDetailIntent)
            getPendingIntent(
                requestCode,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}

/**
 * Data needed to create a notification channel
 *
 * @property id
 * @property name
 * @property description
 * @constructor Create empty Notification channel data
 */
data class NotificationChannelData(
    val id: String,
    val name: String,
    val description: String,
)

/**
 * Data needed to create a notification
 *
 * @property id
 * @property title
 * @property content
 * @property pendingIntent includes deeplink to redirect the user when tapping the notification
 * @property requestCode notification request code
 * @property channelId notification channel id
 * @property date
 * @property setting
 * @property navRoute to navigate to a screen from the NotificationScreen
 */
data class NotificationContent(
    val id: String = "",
    val title: String,
    val content: String,
    val pendingIntent: PendingIntent? = null,
    val requestCode: Int = 0,
    val channelId: String = "",
    val date: Date = Date(),
    val setting: NotificationSetting? = null,
    val navRoute: String? = null
)