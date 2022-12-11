package com.example.hobbyclubs.screens.calendar

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import java.time.LocalDate

/**
 * Calendar screen view model for handling functions related to the calendar
 *
 * @constructor Create empty Calendar screen view model
 */
class CalendarScreenViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val allEvents = MutableLiveData<List<Event>>()
    val selection = MutableLiveData<List<LocalDate>>()

    init {
        getEvents()
    }

    /**
     * Change selection state when user selects/deselects a day within the calendar
     *
     * @param selection Current state of selection in the calendar
     */
    fun onSelectionChanged(selection: List<LocalDate>) {
        if(selection.isNotEmpty()) {
            this.selection.value = selection
        } else {
            this.selection.value = null
        }
    }

    /**
     * Get all events from Firebase
     *
     */
    fun getEvents() {
        firebase.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("calendarViewModel", "getEvents: ", error)
                    return@addSnapshotListener
                }
                val fetchedEvents = data.toObjects(Event::class.java)
                val eventsByDate = fetchedEvents.sortedByDescending { event -> event.date }
                allEvents.value = eventsByDate
            }
    }
}
