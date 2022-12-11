package com.example.hobbyclubs.general

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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

/**
 * Adds the current user's user id to the participants array of an event in firestore.
 * Adds the event reminder alarms accordingly.
 *
 * @param event
 * @param context
 */
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

/**
 * Adds a participation request to an event's request collection
 *
 * @param event
 */
fun createEventRequest(event: Event) {
    val request = EventRequest(
        userId = FirebaseHelper.uid!!,
        acceptedStatus = false,
        timeAccepted = null,
        message = "",
        requestSent = Timestamp.now()
    )
    FirebaseHelper.addEventRequest(eventId = event.id, request = request)
}

/**
 * Removes the current user's user id to the participants array of an event in firestore.
 * Removes the event reminder alarms accordingly.
 *
 * @param event
 * @param context
 */
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

/**
 * Adds or removes the current user's user id to/from the likers array of an event in firestore
 * Updates the event reminder alarms accordingly.
 *
 * @param event
 * @param context
 */
fun updateLikeEvent(event: Event, context: Context) {
    val remove = event.likers.contains(FirebaseHelper.uid)
    FirebaseHelper.updateLikeEvent(eventId = event.id, remove = remove)
    CoroutineScope(Dispatchers.IO).launch {
        val helper = EventAlarmDBHelper(context)
        delay(1000)
        helper.updateAlarms()
    }
}

/**
 * Converts a LocalDate to a string representing that date
 *
 * @return string representing the date
 */
fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

/**
 * Converts a Date to a string that represents the date according to a pattern
 *
 * @param pattern formatting of the date string (e.g. "dd:MM:yyyy")
 * @return a date string
 */
fun Date.toString(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.ENGLISH).format(this)
}

/**
 * Navigate to new screen while popping up to the start destination of the graph
 *
 * @param navController
 * @param route
 */
fun navigateToNewTab(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}