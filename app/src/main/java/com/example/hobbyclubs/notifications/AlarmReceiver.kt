package com.example.hobbyclubs.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Broadcast receiver for event reminder alarms.
 * Creates a notification to remind the user of an upcoming event upon the reception of a broadcast
 *
 */
class AlarmReceiver : BroadcastReceiver() {
    companion object {
        /**
         * Creates the notification channel for event reminders
         *
         * @param context
         */
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

    /**
     * Gets a serializable extra from an intent according to the Android version of the device
     *
     * @param T
     * @param intent
     * @param key
     * @param mClass
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Serializable?> getSerializableExtra(intent: Intent, key: String, mClass: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getSerializableExtra(key, mClass)!!
        else
            intent.getSerializableExtra(key) as T
    }

    /**
     * On the reception of a broadcast, creates a notification with the data received
     *
     * @param p0
     * @param p1
     */
    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let { intent ->
            val data = getSerializableExtra(intent, "data", EventNotificationInfo::class.java)
            p0?.let { context ->
                createNotification(context, data)
            }
        }
    }

    /**
     * Gets the tap pending intent, which allows the user to navigate to the relevant EventScreen
     * when they tap the event reminder notification
     *
     * @param context
     * @param eventId
     * @return a pending intent containing a deeplink to EventScreen with the relevant eventId
     */
    private fun getTapPendingIntent(context: Context, eventId: String): PendingIntent {
        return NotificationHelper.getDeepLinkTapPendingIntent(
            context = context,
            deepLink = "https://hobbyclubs.fi/eventId=$eventId".toUri(),
            requestCode = REQUEST_CODE
        )
    }

    /**
     * Creates the event reminder notification and displays it
     *
     * @param context
     * @param data
     */
    private fun createNotification(context: Context, data: EventNotificationInfo) {
        val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        val timeString = formatter.format(Date(data.eventTime))
        val text =
            "\"${data.eventName}\" starts ${if (data.hoursBefore == 24) "tomorrow" else ""} at $timeString"
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