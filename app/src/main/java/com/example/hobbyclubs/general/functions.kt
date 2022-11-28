package com.example.hobbyclubs.general

import android.content.Context
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.database.EventAlarmDBHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun joinEvent(event: Event, context: Context) {
    val updatedList = event.participants.toMutableList()
    FirebaseHelper.uid?.let {
        updatedList.add(it)
    }
    FirebaseHelper.updateUserInEvent(eventId = event.id, updatedList)
    CoroutineScope(Dispatchers.IO).launch {
        val helper = EventAlarmDBHelper(context)
        delay(1000)
        helper.updateAlarms()
    }
}
fun leaveEvent(event: Event, context: Context) {
    val updatedList = event.participants.toMutableList()
    FirebaseHelper.uid?.let {
        updatedList.remove(it)
    }
    FirebaseHelper.updateUserInEvent(eventId = event.id, updatedList)
    CoroutineScope(Dispatchers.IO).launch {
        val helper = EventAlarmDBHelper(context)
        delay(1000)
        helper.updateAlarms()
    }
}

fun likeEvent(event: Event, context: Context) {
    val initialLikers = event.likers
    FirebaseHelper.uid?.let { uid ->
        val liked = initialLikers.contains(uid)
        val newLikers = if (liked) {
            initialLikers.filter { it != uid }
        } else {
            initialLikers + listOf(uid)
        }
        FirebaseHelper.updateLikeEvent(updatedLikers = newLikers, eventId = event.id)
    }
    CoroutineScope(Dispatchers.IO).launch {
        val helper = EventAlarmDBHelper(context)
        delay(1000)
        helper.updateAlarms()
    }
}



fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())
