package com.example.hobbyclubs.screens.event

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper

class EventScreenViewModel() : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val selectedEventHostClub = MutableLiveData<Club>()

    val isAdmin = Transformations.map(selectedEvent) {
        it.admins.contains(firebase.uid)
    }

    val hasJoinedEvent = Transformations.map(selectedEvent) {
        it.participants.contains(firebase.uid)
    }

    val hasLikedEvent = Transformations.map(selectedEvent) {
        it.likers.contains(firebase.uid)
    }

    fun getEvent(eventId: String) {
        firebase.getEvent(eventId = eventId).get()
            .addOnSuccessListener { data ->
                val fetchedEvent = data.toObject(Event::class.java)
                fetchedEvent?.let {
                    selectedEvent.postValue(it)
                    getEventHostClub(it)
                }
            }
            .addOnFailureListener {
                Log.e("FetchEvent", "getEventFail: ", it)
            }
    }

    fun getEventHostClub(event: Event) {
        firebase.getClub(uid = event.clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let {
                    selectedEventHostClub.postValue(it)
                }
            }
    }

    fun joinEvent(event: Event) {
        val updatedList = event.participants.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.updateUserInEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }

    fun cancelEventJoin(event: Event) {
        val updatedList = event.participants.toMutableList()
        firebase.uid?.let {
            updatedList.remove(it)
        }
        firebase.updateUserInEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }

    fun likeEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }

    fun removeLikeOnEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.remove(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getEvent(event.id)
    }
}