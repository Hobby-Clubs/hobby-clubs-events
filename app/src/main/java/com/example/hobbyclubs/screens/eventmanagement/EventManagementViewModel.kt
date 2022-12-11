package com.example.hobbyclubs.screens.eventmanagement

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import java.util.*

/**
 * Event management view model for handling functions related to the management view of an event
 *
 * @constructor Create empty Event management view model
 */
class EventManagementViewModel : ViewModel() {
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
    val selectedBannerImages = MutableLiveData<List<Uri>?>()
    val givenLinks = MutableLiveData<Map<String, String>>(mapOf())
    val listOfRequests = MutableLiveData<List<EventRequest>>()
    // for event privacy
    val publicSelected = MutableLiveData<Boolean>()
    val privateSelected = MutableLiveData<Boolean>()
    val eventIsPrivate = MutableLiveData<Boolean>()

    /**
     * Fetch the event from Firebase
     *
     * @param eventId UID for the specific event
     */
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

    /**
     * Fill previous event data during editing process
     *
     * @param event Event object
     */
    fun fillPreviousEventData(event: Event) {
        eventName.value = TextFieldValue(event.name)
        eventDescription.value = TextFieldValue(event.description)
        eventContactEmail.value = TextFieldValue(event.contactInfoEmail)
        eventContactNumber.value = TextFieldValue(event.contactInfoNumber)
        eventContactName.value = TextFieldValue(event.contactInfoName)
        eventAddress.value = TextFieldValue(event.address)
        eventDate.value = event.date.toDate()
        eventParticipantLimit.value =
            if (event.participantLimit == -1) TextFieldValue("") else TextFieldValue(event.participantLimit.toString())
        givenLinks.value = event.linkArray
        eventIsPrivate.value = event.isPrivate
        if(event.isPrivate) {
            privateSelected.value = true
            publicSelected.value = false
        }
        else {
            publicSelected.value = true
            privateSelected.value = false
        }
    }

    /**
     * Update edited event name
     *
     * @param newVal New value for event name
     */
    fun updateEventName(newVal: TextFieldValue) {
        eventName.value = newVal
    }

    /**
     * Update edited event description
     *
     * @param newVal New value for event description
     */
    fun updateEventDescription(newVal: TextFieldValue) {
        eventDescription.value = newVal
    }

    /**
     * Update edited event address
     *
     * @param newVal New value for event address
     */
    fun updateEventAddress(newVal: TextFieldValue) {
        eventAddress.value = newVal
    }

    /**
     * Update edited event date
     *
     * @param years
     * @param month
     * @param day
     * @param hour
     * @param minutes
     */
    fun updateEventDate(years: Int, month: Int, day: Int, hour: Int, minutes: Int) {
        eventDate.value = Date(years, month, day, hour, minutes)
    }

    /**
     * Update edited event participant limit
     *
     * @param newVal New value for event participant limit
     */
    fun updateEventParticipantLimit(newVal: TextFieldValue) {
        eventParticipantLimit.value = newVal
    }

    /**
     * Update edited link name
     *
     * @param newVal New value for event link name
     */
    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    /**
     * Update edited link url
     *
     * @param newVal New value for event link URL
     */
    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    /**
     * Add link to a list
     *
     * @param pair Pair of strings, name and URL of link
     */
    fun addLinkToList(pair: Pair<String, String>) {
        givenLinks.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinks.value = newMap

        }
    }

    /**
     * Update edited contact name
     *
     * @param newVal New value for event contact information name
     */
    fun updateContactName(newVal: TextFieldValue) {
        eventContactName.value = newVal
    }

    /**
     * Update edited contact email
     *
     * @param newVal New value for event contact information email
     */
    fun updateContactEmail(newVal: TextFieldValue) {
        eventContactEmail.value = newVal
    }

    /**
     * Update edited contact number
     *
     * @param newVal New value for event contact information phone number
     */
    fun updateContactNumber(newVal: TextFieldValue) {
        eventContactNumber.value = newVal
    }

    /**
     * Clear all link fields
     *
     */
    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    /**
     * Temporarily store selected images
     *
     * @param bannerUri Uri of banner image
     */
    fun temporarilyStoreImages(bannerUri: MutableList<Uri>? = null) {
        bannerUri?.let { selectedBannerImages.value = it }
    }

    /**
     * Remove image from list
     *
     * @param uri Uri of image being removed
     */
    fun removeImageFromList(uri: Uri) {
        val tempList = selectedBannerImages.value?.toMutableList()
        tempList?.let {
            it.remove(uri)
            if (it.isEmpty()) selectedBannerImages.value = null else selectedBannerImages.value =
                it.toList()
        }
    }

    var count = 0

    /**
     * Replace selected event images
     *
     * @param eventId UID for the specific event
     * @param newImages List of uri's for the new images
     */
    fun replaceEventImages(eventId: String, newImages: List<Uri>) {
        val tempList = mutableListOf<Uri>()
        newImages.forEach { uri ->
            firebase.addPic(uri, "${CollectionName.events}/$eventId/$count.jpg")
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        tempList.add(downloadUrl)
                        if (tempList.size == newImages.size) {
                            val changeMap = mapOf(
                                Pair("bannerUris", tempList)
                            )
                            firebase.updateEventDetails(eventId, changeMap)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(FirebaseHelper.TAG, "addPic: ", it)
                }
            count += 1
        }
    }

    /**
     * Delete the event
     *
     * @param eventId UID for the specific event
     */
    fun deleteEvent(eventId: String) {
        firebase.deleteEvent(eventId)
    }

    /**
     * Update the event's information
     *
     * @param eventId UID for the specific event
     * @param changeMap Map of changes
     */
    fun updateEventDetails(eventId: String, changeMap: Map<String, Any>) {
        firebase.updateEventDetails(eventId, changeMap)
    }

    /**
     * Get all join requests from this event
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
     * Update event privacy selection
     * @param leftVal Boolean for public selected
     * @param rightVal Boolean for private selected
     */
    fun updateEventPrivacySelection(leftVal: Boolean, rightVal: Boolean) {
        publicSelected.value = leftVal
        privateSelected.value = rightVal
        eventIsPrivate.value = rightVal
    }
}