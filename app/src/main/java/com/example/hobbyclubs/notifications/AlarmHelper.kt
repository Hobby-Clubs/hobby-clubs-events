package com.example.hobbyclubs.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.hobbyclubs.database.EventAlarmData
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Contains all the functions necessary to set alarms which will trigger event reminder
 * notifications in the future via broadcasting
 *
 * @property context
 */
class AlarmHelper(val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Returns the pending intent necessary to broadcasting a message which will be handled by
     * [AlarmReceiver] in the future.
     * The message contains the information necessary for the an event reminder notification to be
     * displayed.
     *
     * @param data data of the event reminder alarm
     * @param withExtra if true, the pending intent will contain the data of the event reminder alarm
     * @return
     */
    private fun getPendingIntent(data: EventAlarmData, withExtra: Boolean = true): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            if (withExtra) {
                val info = EventNotificationInfo(
                    id = data.id,
                    eventId = data.eventId,
                    eventName = data.eventName,
                    eventTime = data.eventTime.toDate().time,
                    hoursBefore = data.hoursBefore
                )
                putExtra("data", info)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            data.id.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Sets an exact alarm in the future, one hour or one day before an event starts.
     * When the alarm activates, a broadcast message is sent which triggers a notification
     * via the [AlarmReceiver]
     *
     * @param data
     */
    fun setEventAlarm(data: EventAlarmData) {
        val date = data.eventTime.toDate().time - (data.hoursBefore * 3600000)
        val timeString = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
            .format(Date(date))
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            date,
            getPendingIntent(data)
        )
        Log.d("Alarm set", "${data.eventName}: $timeString")
    }

    /**
     * Deletes an event reminder alarm
     *
     * @param data
     */
    fun deleteEventAlarm(data: EventAlarmData) {
        alarmManager.cancel(getPendingIntent(data, false))
    }

    /**
     * Deletes an alarm and replaces it with a new one
     *
     * @param data
     */
    fun updateEventAlarm(data: EventAlarmData) {
        deleteEventAlarm(data)
        setEventAlarm(data)
    }
}

/**
 * Contains all the info needed to display an event reminder notification.
 * Is very similar to [EventAlarmData] but has time expressed as a Long instead of a Timestamp
 * because the latter is not Serializable.
 *
 * @property id
 * @property eventId
 * @property eventName
 * @property eventTime
 * @property hoursBefore
 */
data class EventNotificationInfo(
    val id: Long,
    val eventId: String,
    val eventName: String,
    val eventTime: Long,
    val hoursBefore: Int
): Serializable