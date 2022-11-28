package com.example.hobbyclubs.database

import android.content.Context
import android.util.Log
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.notifications.AlarmHelper
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventAlarmDBHelper(val context: Context) {
    companion object {
        const val TAG = "EventNotificationDBHelper"
    }

    val db = EventAlarmDB.getInstance(context)
    val dao = db.eventNotificationDao()
    val alarmHelper = AlarmHelper(context)

    fun toggleNotifications(isActive: Boolean) {
        if (isActive) {
            updateAlarms()
        } else {
            deleteAllAlarms()
        }
    }

    fun updateAlarms() {
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getAllEvents()
                .get()
                .addOnSuccessListener { value ->
                    val now = Timestamp.now()
                    val events = value.toObjects(Event::class.java).filter { it.date >= now }
                    val joinedEvents = events.filter { it.participants.contains(uid) }
                    val likedEvents = events.filter { it.likers.contains(uid) }
                    val myEvents = joinedEvents + likedEvents.filter {
                        !joinedEvents.map { event -> event.id }.contains(it.id)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        updateDB(myEvents.sortedBy { it.date })
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "compareToFirestore: ", error)
                }
        }
    }

    private fun updateDB(myEventsFromFB: List<Event>) {
        val allEventAlarms = dao.getAllData()
        val deletedEvents =
            allEventAlarms.filter { alarm -> !myEventsFromFB.map { it.id }.contains(alarm.eventId) }
        val newEvents =
            myEventsFromFB.filter { event -> !allEventAlarms.map { it.eventId }.contains(event.id) }
        val toUpdate = getAlarmsToUpdate(myEventsFromFB, allEventAlarms)

        addNewAlarms(newEvents)

        deletedEvents.forEach {
            deleteEventAlarm(it)
        }

        toUpdate.forEach {
            updateEventAlarm(it)
        }
    }

    private fun getAlarmsToUpdate(
        myEventsFromFB: List<Event>,
        alarmsFromDB: List<EventAlarmData>
    ): List<EventAlarmData> {
        val toBeUpdated = alarmsFromDB
            .filter { data -> myEventsFromFB.map { it.id }.contains(data.eventId) }
            .filter { data ->
                val event = myEventsFromFB.find { event -> event.id == data.eventId }
                event?.name != data.eventName ||
                        event.date != data.eventTime
            }
            .map { data ->
                val event = myEventsFromFB.find { event -> event.id == data.eventId }
                EventAlarmData(
                    id = data.id,
                    eventId = data.eventId,
                    eventName = event?.name ?: data.eventName,
                    eventTime = event?.date ?: data.eventTime,
                )
            }
        return toBeUpdated.sortedBy { it.id }
    }

    fun addNewAlarms(events: List<Event>) {
        events.forEach { event ->
            val data = EventAlarmData(
                id = 0,
                eventId = event.id,
                eventTime = event.date,
                eventName = event.name,
            )
            addEventAlarm(data)
        }
    }

    fun addEventAlarm(data: EventAlarmData) {
        dao.addData(data)
        alarmHelper.setEventAlarm(data)
    }

    fun updateEventAlarm(data: EventAlarmData) {
        dao.updateData(data)
        alarmHelper.updateEventAlarm(data)
    }

    fun deleteAllAlarms() {
        CoroutineScope(Dispatchers.IO).launch {
            val allAlarms = dao.getAllData()
            allAlarms.forEach {
                deleteEventAlarm(it)
            }
        }
    }

    fun deleteEventAlarm(data: EventAlarmData) {
        dao.removeData(data)
        alarmHelper.deleteEventAlarm(data)
    }
}