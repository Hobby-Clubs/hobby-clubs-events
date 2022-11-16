package com.example.hobbyclubs.screens.event

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper

class EventScreenViewModel() : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()
    val imagesUri = MutableLiveData<Uri>()

    fun getEvent(eventId: String) {
        firebase.getEvent(uid = eventId).get()
            .addOnSuccessListener { data ->
                val fetchedEvent = data.toObject(Event::class.java)
                fetchedEvent?.let { selectedEvent.postValue(fetchedEvent) }
            }
            .addOnFailureListener {
                Log.e("FetchEvent", "getEventFail: ", it)
            }
    }

    fun getImages(eventId: String) {
        FirebaseHelper.getFile("${CollectionName.clubs}/$eventId/").downloadUrl
            .addOnSuccessListener { imagesUri.value = it }
    }
}