package com.example.hobbyclubs.screens.create.event

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.firestore.FieldValue
import java.util.*

/**
 * Create event view model for handling event creation functions
 */
class CreateEventViewModel : ViewModel() {

    val firebase = FirebaseHelper
    val currentUser = MutableLiveData<User>()
    val joinedClubs = MutableLiveData<List<Club>>()

    val currentCreationProgressPage = MutableLiveData<Int>()

    // club selected to post the event for
    val selectedClub = MutableLiveData<String>()

    // event details
    val eventName = MutableLiveData<TextFieldValue?>()
    val eventDescription = MutableLiveData<TextFieldValue>()
    val eventLocation = MutableLiveData<TextFieldValue>()
    val eventParticipantLimit = MutableLiveData<TextFieldValue>()
    val selectedDate = MutableLiveData<Date>()
    val selectedImages = MutableLiveData<List<Uri>?>()

    // contact information
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()

    // socials
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val givenLinksLiveData = MutableLiveData<Map<String, String>>(mapOf())

    // for event privacy
    val publicSelected = MutableLiveData<Boolean>()
    val privateSelected = MutableLiveData<Boolean>()
    val eventIsPrivate = MutableLiveData<Boolean>()

    init {
        getJoinedClubs()
    }

    /**
     * Change to wanted page
     * @param page page number to change to
     */
    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }

    /**
     * Update eventName value based on newVal
     * @param newVal value to put into eventName
     */
    fun updateEventName(newVal: TextFieldValue) {
        eventName.value = newVal
    }

    /**
     * Update selected club value based on newVal
     * @param newVal value to put into selectedClub
     */
    fun updateSelectedClub(newVal: String) {
        selectedClub.value = newVal
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

    /**
     * Update selected date
     *
     * @param years provided year value
     * @param month provided month value
     * @param day provided day value
     * @param hour provided hour value
     * @param minutes provided minute value
     */
    fun updateSelectedDate(years: Int, month: Int, day: Int, hour: Int, minutes: Int) {
        selectedDate.value = Date(years, month, day, hour, minutes)
    }

    /**
     * Update event description based on newVal
     * @param newVal value to put into eventDescription
     */
    fun updateEventDescription(newVal: TextFieldValue) {
        eventDescription.value = newVal
    }

    /**
     * Update event participant limit based on newVal
     * @param newVal value to put into eventParticipantLimit
     */
    fun updateEventParticipantLimit(newVal: TextFieldValue) {
        eventParticipantLimit.value = newVal
    }

    /**
     * Update event location based on newVal
     * @param newVal value to put into eventLocation
     */
    fun updateEventLocation(newVal: TextFieldValue) {
        eventLocation.value = newVal
    }

    /**
     * Update contact info name based on newVal
     * @param newVal value to put into contactInfoName
     */
    fun updateContactInfoName(newVal: TextFieldValue) {
        contactInfoName.value = newVal
    }

    /**
     * Update contact info email based on newVal
     * @param newVal value to put into contactInfoEmail
     */
    fun updateContactInfoEmail(newVal: TextFieldValue) {
        contactInfoEmail.value = newVal
    }

    /**
     * Update contact info number based on newVal
     * @param newVal value to put into contactInfoNumber
     */
    fun updateContactInfoNumber(newVal: TextFieldValue) {
        contactInfoNumber.value = newVal
    }

    /**
     * Update current link name based on newVal
     * @param newVal value to put into currentLinkName
     */
    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    /**
     * Update current link url based on newVal
     * @param newVal value to put into currentLinkURL
     */
    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    /**
     * Get clubs the logged in user has joined and add General Club to show general option
     */
    private fun getJoinedClubs() {
        firebase.uid?.let {
            firebase.getAllClubs().whereArrayContains("members", it).get()
                .addOnSuccessListener { clubs ->
                    val fetchedJoinedClubs = clubs.toObjects(Club::class.java)
                    val generalClub = Club(name = "General")
                    fetchedJoinedClubs.add(generalClub)
                    joinedClubs.value = fetchedJoinedClubs.reversed()
                }
        }
    }

    /**
     * Add link to list to post on creation
     * @param pair pair of link name and url address
     */
    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap
        }
    }

    /**
     * Clear link fields after adding pair to given links list
     */
    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    /**
     * Add event to firebase
     * @param event Event object to add
     * @return returns eventId
     */
    fun addEvent(event: Event): String {
        return firebase.addEvent(event)
    }

    // for storing multiple images with different name
    private var count = 0

    /**
     * Store images on firebase and update events bannerUris field with URI.
     * @param listToStore list of uris to store on firebase
     * @param eventId UID for the selected event
     */
    fun storeImagesOnFirebase(listToStore: List<Uri>, eventId: String) {
        listToStore.forEach { uri ->
            firebase.addPic(uri, "${CollectionName.events}/$eventId/$count.jpg")
                .addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val changeMap = mapOf(
                            Pair("bannerUris", FieldValue.arrayUnion(downloadUrl))
                        )
                        firebase.updateEventDetails(eventId, changeMap)
                    }
                }
                .addOnFailureListener {
                    Log.e(FirebaseHelper.TAG, "addPic: ", it)
                }
            count += 1
        }
    }

    /**
     * Temporarily store images in a MutableLiveData for showing them on the screen before actual creation.
     * @param images list of Uris to put into selectedImages
     */
    fun temporarilyStoreImages(images: MutableList<Uri>) {
        selectedImages.value = images
    }

    /**
     * Remove image from selected images list
     * @param uri uri to remove from list
     */
    fun removeImageFromList(uri: Uri) {
        val tempList =  selectedImages.value?.toMutableList()
        tempList?.let {
            it.remove(uri)
            if (it.isEmpty()) selectedImages.value = null else selectedImages.value = it.toList()
        }
    }

    /**
     * Get current users data from firebase
     */
    fun getCurrentUser() {
        firebase.getCurrentUser().get()
            .addOnSuccessListener { data ->
                val fetchedUser = data.toObject(User::class.java)
                fetchedUser?.let { currentUser.postValue(it) }
            }
            .addOnFailureListener {
                Log.e("FetchUser", "getUserFail: ", it)
            }
    }

    /**
     * Quick fill options for contact information text fields
     * @param user User object to take information from
     */
    fun quickFillOptions(user: User) {
        contactInfoName.value = TextFieldValue("${user.fName} ${user.lName}")
        contactInfoEmail.value = TextFieldValue(user.email)
        contactInfoNumber.value = TextFieldValue(user.phone)
    }
}