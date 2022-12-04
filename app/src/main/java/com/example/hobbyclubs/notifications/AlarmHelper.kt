package com.example.hobbyclubs.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.hobbyclubs.database.EventAlarmData
import com.google.common.math.LongMath
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class AlarmHelper(val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

    fun deleteEventAlarm(data: EventAlarmData) {
        alarmManager.cancel(getPendingIntent(data, false))
    }

    fun updateEventAlarm(data: EventAlarmData) {
        deleteEventAlarm(data)
        setEventAlarm(data)
    }
}

data class EventNotificationInfo(
    val id: Long,
    val eventId: String,
    val eventName: String,
    val eventTime: Long,
    val hoursBefore: Int
): Serializable