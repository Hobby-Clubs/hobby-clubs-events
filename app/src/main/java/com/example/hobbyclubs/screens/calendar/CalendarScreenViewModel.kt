package com.example.hobbyclubs.screens.calendar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.toDate
import com.example.hobbyclubs.screens.clubs.ClubsScreenViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.util.*

class CalendarScreenViewModel() : ViewModel() {
    val firebase = FirebaseHelper

    val allEvents = MutableLiveData<List<Event>>()
    val filteredEvents = MutableLiveData<List<Event>>()

    init {
        getEvents()
    }

    fun onSelectionChanged(selection: List<LocalDate>) {
        if(!selection.isEmpty()) {
            filteredEvents.value = allEvents.value?.filter { event ->
                event.date.toDate() == selection.first().toDate()
            }
        } else {
            filteredEvents.value = emptyList()
        }
    }

    fun getEvents() {
        firebase.getAllEvents()
            .get()
            .addOnSuccessListener listener@ { events ->
                val fetchedEvents = events.toObjects(Event::class.java)
                if (fetchedEvents.isEmpty()) {
                    return@listener
                }
                val eventsByDate = fetchedEvents.sortedByDescending { event -> event.date }
                allEvents.value = eventsByDate
                Log.d("events", eventsByDate.toString())
            }
            .addOnFailureListener { error ->
                Log.e("EventScreenViewModel", "getEvents: ", error)
            }
    }

    fun getFilteredList(d: Date): LiveData<List<Event>> {
        return Transformations.map(allEvents) { event ->
            event.filter {
                it.date.toDate() == d
            }
        }
    }
}
