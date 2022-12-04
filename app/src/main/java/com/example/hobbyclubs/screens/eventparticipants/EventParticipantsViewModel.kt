package com.example.hobbyclubs.screens.eventparticipants

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

class EventParticipantsViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val listOfParticipants = MutableLiveData<List<User>>(listOf())
    val listOfAdmins = MutableLiveData<List<User>>(listOf())
    val listOfRequests = MutableLiveData<List<EventRequest>>(listOf())

    fun getEventParticipants(eventParticipants: List<String>) {
        listOfParticipants.value = listOf()
        eventParticipants.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    Log.d("getParticipants", it.toString())
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfParticipants.value = listOfParticipants.value?.plus(listOf(user))
                    }
                    Log.d("getParticipants", listOfParticipants.value.toString())
                }
                .addOnFailureListener { e ->
                    Log.e("getParticipants", "getParticipants failed: ", e)
                }
        }
    }

    fun getEventAdmins(eventAdmins: List<String>) {
        listOfAdmins.value = listOf()
        eventAdmins.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    Log.d("getEventAdmins", it.toString())
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfAdmins.value = listOfAdmins.value?.plus(listOf(user))
                    }
                    Log.d("getEventAdmins", listOfAdmins.value.toString())
                }
                .addOnFailureListener { e ->
                    Log.e("getEventAdmins", "getEventAdmins failed: ", e)
                }
        }
    }

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

    fun getAllJoinRequests(eventId: String) {
        firebase.getRequestsFromEvent(eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(EventRequest::class.java)
                Log.d("fetchRequests", fetchedRequests.toString())
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

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

    fun declineJoinRequest(eventId: String, requestId: String) {
        firebase.declineEventRequest(eventId, requestId)
    }
}