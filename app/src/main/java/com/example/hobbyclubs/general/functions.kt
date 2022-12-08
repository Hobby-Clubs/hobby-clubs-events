package com.example.hobbyclubs.general

import android.content.Context
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.EventRequest
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.database.EventAlarmDBHelper
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun joinEvent(event: Event, context: Context) {
    FirebaseHelper.uid?.let { uid ->
        FirebaseHelper.updateJoinEvent(eventId = event.id, uid)
        CoroutineScope(Dispatchers.IO).launch {
            val helper = EventAlarmDBHelper(context)
            delay(1000)
            helper.updateAlarms()
        }
    }
}
fun createEventRequest(event: Event, context: Context) {
    val request = EventRequest(
        userId = FirebaseHelper.uid!!,
        acceptedStatus = false,
        timeAccepted = null,
        message = "",
        requestSent = Timestamp.now()
    )
    FirebaseHelper.addEventRequest(eventId = event.id, request = request)
}
fun leaveEvent(event: Event, context: Context) {
    FirebaseHelper.uid?.let { uid ->
        FirebaseHelper.updateJoinEvent(eventId = event.id, userId = uid, remove = true)
        CoroutineScope(Dispatchers.IO).launch {
            val helper = EventAlarmDBHelper(context)
            delay(1000)
            helper.updateAlarms()
        }
    }
}

fun updateLikeEvent(event: Event, context: Context) {
    val remove = event.likers.contains(FirebaseHelper.uid)
    FirebaseHelper.updateLikeEvent(eventId = event.id, remove = remove)
    CoroutineScope(Dispatchers.IO).launch {
        val helper = EventAlarmDBHelper(context)
        delay(1000)
        helper.updateAlarms()
    }
}

fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

fun Date.toString(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.ENGLISH).format(this)
}