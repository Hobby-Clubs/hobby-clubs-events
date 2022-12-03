package com.example.hobbyclubs.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hobbyclubs.MainActivity
import com.example.hobbyclubs.R
import com.example.hobbyclubs.screens.settings.NotificationSetting
import kotlinx.parcelize.Parcelize
import java.util.*

object NotificationHelper {
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

data class NotificationChannelData(
    val id: String,
    val name: String,
    val description: String,
)

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