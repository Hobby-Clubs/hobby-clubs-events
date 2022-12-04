package com.example.hobbyclubs.screens.eventmanagement

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import java.util.*

class EventManagementViewModel() : ViewModel() {
    val firebase = FirebaseHelper
    val selectedEvent = MutableLiveData<Event>()

    val eventName = MutableLiveData<TextFieldValue>()
    val eventDescription = MutableLiveData<TextFieldValue>()
    val eventAddress = MutableLiveData<TextFieldValue>()
    val eventParticipantLimit = MutableLiveData<TextFieldValue>()
    val eventDate = MutableLiveData<Date>()
    val eventContactName = MutableLiveData<TextFieldValue>()
    val eventContactEmail = MutableLiveData<TextFieldValue>()
    val eventContactNumber = MutableLiveData<TextFieldValue>()
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val selectedBannerImages = MutableLiveData<MutableList<Uri>>()
    val givenLinks = MutableLiveData<Map<String, String>>(mapOf())

    fun getEvent(eventId: String) {
        firebase.getEvent(eventId = eventId).get()
            .addOnSuccessListener { data ->
                val fetchedEvent = data.toObject(Event::class.java)
                fetchedEvent?.let {
                    selectedEvent.postValue(it)
                }
            }
            .addOnFailureListener {
                Log.e("FetchEvent", "getEventFail: ", it)
            }
    }

    fun fillPreviousEventData(event: Event) {
        eventName.value = TextFieldValue(event.name)
        eventDescription.value  = TextFieldValue(event.description)
        eventContactEmail.value  = TextFieldValue(event.contactInfoEmail)
        eventContactNumber.value  = TextFieldValue(event.contactInfoNumber)
        eventContactName.value  = TextFieldValue(event.contactInfoName)
        eventAddress.value = TextFieldValue(event.address)
        eventDate.value = event.date.toDate()
        eventParticipantLimit.value = TextFieldValue(event.participantLimit.toString())
        givenLinks.value = event.linkArray

    }

    fun updateEventName(newVal: TextFieldValue) {
        eventName.value = newVal
    }

    fun updateEventDescription(newVal: TextFieldValue) {
        eventDescription.value = newVal
    }

    fun updateEventAddress(newVal: TextFieldValue) {
        eventAddress.value = newVal
    }

    fun updateEventDate(years: Int, month: Int, day: Int, hour: Int, minutes: Int) {
        eventDate.value = Date(years, month, day, hour,minutes)
        Log.d("dateSelection", eventDate.value.toString())
    }

    fun updateEventParticipantLimit(newVal: TextFieldValue) {
        eventParticipantLimit.value = newVal
    }

    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    fun addLinkToList(pair: Pair<String, String>) {
        givenLinks.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinks.value = newMap

        }
    }

    fun updateContactName(newVal: TextFieldValue) {
        eventContactName.value = newVal
    }

    fun updateContactEmail(newVal: TextFieldValue) {
        eventContactEmail.value = newVal
    }

    fun updateContactNumber(newVal: TextFieldValue) {
        eventContactNumber.value = newVal
    }

    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    fun temporarilyStoreImages(bannerUri: MutableList<Uri>? = null) {
        bannerUri?.let { selectedBannerImages.value = it }
    }

    fun replaceEventImages(eventId: String, newImages: List<Uri>) {
        firebase.updateEventImages(eventId, newImages)
    }

    fun deleteEvent(eventId: String) {
        firebase.deleteEvent(eventId)
    }

    fun updateEventDetails(eventId: String, changeMap: Map<String,Any>) {
        firebase.updateEventDetails(eventId, changeMap)
    }
}