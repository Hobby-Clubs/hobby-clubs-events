package com.example.hobbyclubs.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        fun createNotificationChannel(context: Context) {
            val data = NotificationChannelData(
                "events",
                "Event reminders",
                "Notifications for event reminders"
            )
            NotificationHelper.createNotificationChannel(
                context,
                data
            )
        }

        const val REQUEST_CODE = 934829
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        val data = p1?.getSerializableExtra("data") as? EventNotificationInfo

        p0?.let { context ->
            data?.let {
                Log.d("AlarmReceiver", "onReceive: ${it.eventName}")
                createNotification(context, it)
            }
        }

        val message = p1?.getStringExtra("message")

        message?.let {
            Log.d("AlarmReceiver", "onReceive: $it")
        }
    }

    private fun getTapPendingIntent(context: Context, eventId: String): PendingIntent {
       return NotificationHelper.getDeepLinkTapPendingIntent(
            context = context,
            deepLink = "https://hobbyclubs.fi/eventId=$eventId".toUri(),
            requestCode = REQUEST_CODE
        )
    }

    private fun createNotification(context: Context, data: EventNotificationInfo) {
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val timeString = formatter.format(Date(data.eventTime))
        val text = "\"${data.eventName}\" starts ${if (data.hoursBefore == 24) "tomorrow" else ""} at $timeString"
        val notifData = NotificationContent(
            title = "Event reminder",
            content = text,
            pendingIntent = getTapPendingIntent(context, data.eventId),
            requestCode = REQUEST_CODE,
            channelId = "events"
        )
        NotificationHelper.createNotification(context, notifData)
    }
}