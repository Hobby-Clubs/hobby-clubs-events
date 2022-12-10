package com.example.hobbyclubs.screens.eventparticipants

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

/**
 * Event participants view model for handling functions related to the screen displaying a list of participants of an event
 *
 * @constructor Create empty Event participants view model
 */
class EventParticipantsViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val listOfParticipants = MutableLiveData<List<User>>(listOf())
    val listOfAdmins = MutableLiveData<List<User>>(listOf())
    val listOfRequests = MutableLiveData<List<EventRequest>>(listOf())

    /**
     * Get an updated list of all of the event's participants
     *
     * @param eventParticipants List of the event's participants
     */
    fun getEventParticipants(eventParticipants: List<String>) {
        listOfParticipants.value = listOf()
        eventParticipants.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfParticipants.value = listOfParticipants.value?.plus(listOf(user))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("getParticipants", "getParticipants failed: ", e)
                }
        }
    }

    /**
     * Get an updated list of the event's admins
     *
     * @param eventAdmins List of admins
     */
    fun getEventAdmins(eventAdmins: List<String>) {
        listOfAdmins.value = listOf()
        eventAdmins.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfAdmins.value = listOfAdmins.value?.plus(listOf(user))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("getEventAdmins", "getEventAdmins failed: ", e)
                }
        }
    }

    /**
     * Fetch the specific event and update the list of participants and admins of that event
     *
     * @param eventId UID for the specific event
     */
    fun getEvent(eventId: String) {
        firebase.getEvent(eventId = eventId).addSnapshotListener { data, e ->
            data ?: run {
                Log.e("fetchEvent", "getEventFailed: ", e)
                return@addSnapshotListener
            }
            val fetchedEvent = data.toObject(Event::class.java)
            fetchedEvent?.let {
                selectedEvent.postValue(it)
                getEventParticipants(it.participants)
                getEventAdmins(it.admins)
            }
        }
    }

    /**
     * Fetch all of the join requests to a specific event
     *
     * @param eventId UID for the specific event
     */
    fun getAllJoinRequests(eventId: String) {
        firebase.getRequestsFromEvent(eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(EventRequest::class.java)
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

    /**
     * Accept a join request to an event
     *
     * @param eventId UID for the specific event
     * @param requestId UID for the specific event request
     * @param memberListWithNewUser List of members with the new user
     * @param changeMapForRequest Map of changes for the request object
     */
    fun acceptJoinRequest(
        eventId: String,
        requestId: String,
        memberListWithNewUser: List<String>,
        changeMapForRequest: Map<String, Any>
    ) {
        firebase.acceptEventRequest(
            eventId = eventId,
            requestId = requestId,
            memberListWithNewUser = memberListWithNewUser,
            changeMapForRequest = changeMapForRequest
        )
    }

    /**
     * Decline a join request to an event
     *
     * @param eventId UID for the specific event
     * @param requestId UID for the specific event request
     */
    fun declineJoinRequest(eventId: String, requestId: String) {
        firebase.declineEventRequest(eventId, requestId)
    }
}