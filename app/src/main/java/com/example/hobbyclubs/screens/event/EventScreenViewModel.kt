package com.example.hobbyclubs.screens.event

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp

class EventScreenViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val selectedEventHostClub = MutableLiveData<Club>()
    val eventRequests = MutableLiveData<List<EventRequest>>()

    val isAdmin = Transformations.map(selectedEvent) {
        it.admins.contains(firebase.uid)
    }

    val hasRequested = Transformations.map(eventRequests) { list ->
        list.any { it.userId == firebase.uid && !it.acceptedStatus }
    }

    val hasJoinedEvent = Transformations.map(selectedEvent) {
        it.participants.contains(firebase.uid)
    }

    val hasLikedEvent = Transformations.map(selectedEvent) {
        it.likers.contains(firebase.uid)
    }

    fun getEvent(eventId: String) {
        firebase.getEvent(eventId = eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getEvent", "getEvent: ", error )
                    return@addSnapshotListener
                }
                val fetchedEvent = data.toObject(Event::class.java)
                fetchedEvent?.let {
                    selectedEvent.postValue(it)
                    getEventHostClub(it)
                }
            }
    }

    fun getEventHostClub(event: Event) {
        firebase.getClub(clubId = event.clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let {
                    selectedEventHostClub.postValue(it)
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
        Toast.makeText(context, "Request sent", Toast.LENGTH_LONG).show()
    }

    fun getEventJoinRequests(eventId: String) {
        firebase.getRequestsFromEvent(eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(EventRequest::class.java)
                Log.d("fetchRequests", fetchedRequests.toString())
                eventRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}