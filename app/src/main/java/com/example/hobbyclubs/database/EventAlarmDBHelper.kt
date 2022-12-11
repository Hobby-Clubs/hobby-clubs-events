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

/**
 * Class which helps setting up/updating/deleting event reminder alarms (see [EventAlarmData])
 * while keeping track of them in [EventAlarmDB]
 *
 */
class EventAlarmDBHelper(val context: Context) {
    companion object {
        const val TAG = "EventNotificationDBHelper"
    }

    private val db = EventAlarmDB.getInstance(context)
    private val dao = db.eventNotificationDao()
    private val alarmHelper = AlarmHelper(context)
    private val settingsPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /**
     * Checks the event reminder notification settings (1 hour before and/or 1 day before).
     * Deletes the unnecessary alarms if notification settings have changed.
     * If any reminder is required, fetches the relevant events (joined or liked), and passes
     * them to [updateDB]
     */
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

    /**
     * Compares the existing event alarms to the events present on firestore, then deletes the
     * alarms for events that were deleted in firestore, adds the relevant alarms for new events,
     * and updates the event information in the EventAlarmDatabase (start time and name)
     * if they have changed.
     * For each modification made in the database, an equivalent action is performed on the alarms.
     *
     * @param myEventsFromFB relevant events from firebase (liked or joined)
     * @param needsHourReminder true if 1-hour event notifications are enabled
     * @param needsDayReminder true if 1-day event notifications are enabled
     */
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

    /**
     * Removes alarms both from the AlarmManager of the device and the EventAlarmDB, if either
     * 1-hour reminders or 1-day reminders (or both) are no longer needed
     *
     * @param needsHourReminder true if 1-hour event notifications are enabled
     * @param needsDayReminder true if 1-day event notifications are enabled
     */
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

    /**
     * Compares the EventAlarmData contained in [EventAlarmDB] to the event data in firestore
     * and returns a list of updated data for the EventAlarmData objects to be modified.
     *
     * @param myEventsFromFB
     * @param alarmsFromDB
     * @return a list of updated EventAlarmData
     */
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

    /**
     * For each event in firestore, sets any missing alarm with AlarmManager and adds them to
     * EventAlarmDB.
     * Alarms can be missing if they have just been added to firestore or if the notification settings
     * have changed (e.g. the user now wants a notification 1 hour before the events)
     *
     * @param events
     * @param allEventAlarms
     * @param needsHourReminder
     * @param needsDayReminder
     */
    private fun addNewAlarms(
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

    /**
     * Sets an alarm that will trigger a notification before an event and keeps track of it in the
     * EventAlarmDB
     *
     * @param data Data for the event alarm to be added
     */
    private fun addEventAlarm(data: EventAlarmData) {
        dao.addData(data)
        alarmHelper.setEventAlarm(data)
    }

    /**
     * Updates an alarm that will trigger a notification before an event and keeps track of it in the
     * EventAlarmDB
     *
     * @param data Data for the event alarm to be added
     */
    private fun updateEventAlarm(data: EventAlarmData) {
        dao.updateData(data)
        alarmHelper.updateEventAlarm(data)
    }

    /**
     * Cancels a set event alarm as well as its counterpart in the EventAlarmDB
     *
     * @param data
     */
    private fun deleteEventAlarm(data: EventAlarmData) {
        dao.removeData(data)
        alarmHelper.deleteEventAlarm(data)
    }
}