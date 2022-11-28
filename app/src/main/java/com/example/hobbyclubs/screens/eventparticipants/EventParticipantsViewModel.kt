package com.example.hobbyclubs.screens.eventparticipants

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User

class EventParticipantsViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val listOfParticipants = MutableLiveData<List<User>>(listOf())

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
            }
        }
    }


}