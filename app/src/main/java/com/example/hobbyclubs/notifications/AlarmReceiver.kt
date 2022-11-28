package com.example.hobbyclubs.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.example.hobbyclubs.MainActivity
import com.example.hobbyclubs.R
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        fun createNotificationChannel(context: Context?) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                "events",
                "Events Notification Channel",
                importance
            ).apply {
                description = "Notification for Events"
            }
            context?.let {
                val notificationManager =
                    it.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private var notificationManager: NotificationManagerCompat? = null

    override fun onReceive(p0: Context?, p1: Intent?) {
        val data = p1?.getSerializableExtra("data") as? EventNotificationInfo

        data?.let {
            Log.d("AlarmReceiver", "onReceive: ${it.eventName}")
            createNotification(p0, it)
        }

        val message = p1?.getStringExtra("message")

        message?.let {
            Log.d("AlarmReceiver", "onReceive: $it")
        }
    }

    private fun getTapPendingIntent(context: Context?, eventId: String): PendingIntent? {
        val taskDetailIntent = Intent(
            Intent.ACTION_VIEW,
            "https://hobbyclubs.fi/eventId=$eventId".toUri(),
            context,
            MainActivity::class.java
        )

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(taskDetailIntent)
            getPendingIntent(897325, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        }
    }

    private fun createNotification(context: Context?, data: EventNotificationInfo) {
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val timeString = formatter.format(Date(data.eventTime))
        val text = "\"${data.eventName}\" starts at $timeString"
        val notification = context?.let {
            NotificationCompat.Builder(it, "events")
                .setContentTitle("Event reminder")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getTapPendingIntent(it, data.eventId))
                .build()
        }
        notificationManager = context?.let { NotificationManagerCompat.from(it) }
        notification?.let { notif ->
            notificationManager?.notify(data.id.toInt(), notif)
        }
    }
}