package com.example.hobbyclubs.database

import android.content.Context
import android.util.Log
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.notifications.AlarmHelper
import com.example.hobbyclubs.screens.settings.NotificationSetting
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
    val settingsPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun updateAlarms() {
        val needsHourReminder =
            settingsPref.getBoolean(NotificationSetting.EVENT_HOUR_REMINDER.name, false)
        val needsDayReminder = settingsPref.getBoolean(NotificationSetting.EVENT_DAY_REMINDER.name, false)
        deleteExtraAlarms(needsHourReminder, needsDayReminder)

        if (!needsHourReminder && needsDayReminder) {
            return
        }

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
                        updateDB(myEvents.sortedBy { it.date }, needsHourReminder, needsDayReminder)
                    }
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "compareToFirestore: ", error)
                }
        }
    }

    private fun updateDB(
        myEventsFromFB: List<Event>,
        needsHourReminder: Boolean,
        needsDayReminder: Boolean
    ) {
        val allEventAlarms = dao.getAllData()
        val hourAlarms = allEventAlarms.filter { it.hoursBefore == 1 }
        val dayAlarms = allEventAlarms.filter { it.hoursBefore == 24 }

        val toDelete =
            allEventAlarms.filter { alarm -> !myEventsFromFB.map { it.id }.contains(alarm.eventId) }

        val toAdd =
            myEventsFromFB.filter { event ->
                !hourAlarms.map { it.eventId }.contains(event.id)
                        || !dayAlarms.map { it.eventId }.contains(event.id)
            }

        val toUpdate = getAlarmsToUpdate(myEventsFromFB, allEventAlarms)

        toDelete.forEach {
            deleteEventAlarm(it)
        }

        toUpdate.forEach {
            updateEventAlarm(it)
        }

        addNewAlarms(toAdd, allEventAlarms, needsHourReminder, needsDayReminder)
    }

    private fun deleteExtraAlarms(needsHourReminder: Boolean, needsDayReminder: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val allEventAlarms = dao.getAllData()
            val hoursAlarms = allEventAlarms.filter { it.hoursBefore == 1 }
            val dayAlarms = allEventAlarms.filter { it.hoursBefore == 24 }
            if (!needsHourReminder) {
                hoursAlarms.forEach { deleteEventAlarm(it) }
            }
            if (!needsDayReminder) {
                dayAlarms.forEach { deleteEventAlarm(it) }
            }
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
                    hoursBefore = data.hoursBefore
                )
            }
        return toBeUpdated.sortedBy { it.id }
    }

    fun addNewAlarms(
        events: List<Event>,
        allEventAlarms: List<EventAlarmData>,
        needsHourReminder: Boolean,
        needsDayReminder: Boolean
    ) {
        if (!needsHourReminder && !needsDayReminder) {
            return
        }

        val hourAlarms = allEventAlarms.filter { it.hoursBefore == 1 }
        val dayAlarms = allEventAlarms.filter { it.hoursBefore == 24 }

        events.forEach { event ->
            val hasHourAlarm = hourAlarms.map { it.eventId }.contains(event.id)
            val hasDayAlarm = dayAlarms.map { it.eventId }.contains(event.id)

            if (needsHourReminder && !hasHourAlarm) {
                val data = EventAlarmData(
                    id = 0,
                    eventId = event.id,
                    eventTime = event.date,
                    eventName = event.name,
                    hoursBefore = 1
                )
                addEventAlarm(data)
            }

            if (needsDayReminder && !hasDayAlarm) {
                val data = EventAlarmData(
                    id = 0,
                    eventId = event.id,
                    eventTime = event.date,
                    eventName = event.name,
                    hoursBefore = 24
                )
                addEventAlarm(data)
            }
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