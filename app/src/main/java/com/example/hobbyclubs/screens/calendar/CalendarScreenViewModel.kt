package com.example.hobbyclubs.screens.calendar

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.toDate
import org.joda.time.DateTimeComparator
import java.time.LocalDate

class CalendarScreenViewModel() : ViewModel() {
    val firebase = FirebaseHelper
    val dateTimeComparator = DateTimeComparator.getDateOnlyInstance()

    val allEvents = MutableLiveData<List<Event>>()
    val filteredEvents = MutableLiveData<List<Event>>()

    init {
        getEvents()
    }

    fun onSelectionChanged(selection: List<LocalDate>) {
        if(!selection.isEmpty()) {
            filteredEvents.value = allEvents.value?.filter { event ->
                (dateTimeComparator.compare(event.date.toDate(), selection.first().toDate())) == 0
                // event.date.toDate() == selection.first().toDate()
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

    fun joinEvent(event: Event) {
        val updatedList = event.participants.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserToEvent(eventId = event.id, updatedList)
        getEvents()
    }

    fun likeEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvents()
    }

    fun removeLikeOnEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.remove(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvents()
    }

    fun getEvent(eventId: String) = firebase.getEvent(eventId)
}
