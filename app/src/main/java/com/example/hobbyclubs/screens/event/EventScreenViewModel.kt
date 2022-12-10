package com.example.hobbyclubs.screens.event

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp

/**
 * Event screen view model for handling functions related to the detailed view of an event
 *
 * @constructor Create empty Event screen view model
 */
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

    /**
     * Fetch the event from Firebase
     *
     * @param eventId UID for the event displayed
     */
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

    /**
     * Get the event's host club
     *
     * @param event Event object
     */
    fun getEventHostClub(event: Event) {
        firebase.getClub(uid = event.clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let {
                    selectedEventHostClub.postValue(it)
                }
            }
    }

    /**
     * Like the event
     *
     * @param event Event object
     */
    fun likeEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }

    /**
     * Remove like on event
     *
     * @param event Event object
     */
    fun removeLikeOnEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.remove(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }

    /**
     * Join an event
     *
     * @param eventId UID for the specific event
     */
    fun joinEvent(eventId: String) {
        val updatedList = selectedEvent.value?.participants?.toMutableList()
        firebase.uid?.let {
            updatedList?.add(it)
        }
        firebase.updateUserInEvent(eventId = eventId, updatedList!!)
        getEvent(eventId)
    }

    /**
     * Leave an event
     *
     * @param eventId UID for the specific event
     */
    fun leaveEvent(eventId: String) {
        val updatedList = selectedEvent.value?.participants?.toMutableList()
        firebase.uid?.let {
            updatedList?.remove(it)
        }
        firebase.updateUserInEvent(eventId = eventId, updatedList!!)
        getEvent(eventId)
    }

    /**
     * Create a join request to an event
     *
     * @param event Event object
     * @param context LocalContext
     */
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

    /**
     * Get all join requests to an event
     *
     * @param eventId UID for the specific event
     */
    fun getEventJoinRequests(eventId: String) {
        firebase.getRequestsFromEvent(eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(EventRequest::class.java)
                eventRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}