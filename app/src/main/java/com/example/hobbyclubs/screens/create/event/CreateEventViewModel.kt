package com.example.hobbyclubs.screens.create.event

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.firestore.FieldValue
import java.util.*

class CreateEventViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentUser = MutableLiveData<User>()
    val currentCreationProgressPage = MutableLiveData<Int>()
    val selectedClub = MutableLiveData<String>()
    val selectedDate = MutableLiveData<Date>()
    val eventName = MutableLiveData<TextFieldValue?>()
    val eventDescription = MutableLiveData<TextFieldValue>()
    val eventLocation = MutableLiveData<TextFieldValue>()
    val eventParticipantLimit = MutableLiveData<TextFieldValue>()
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val leftSelected = MutableLiveData<Boolean>()
    val rightSelected = MutableLiveData<Boolean>()
    val eventIsPrivate = MutableLiveData<Boolean>()

    val selectedImages = MutableLiveData<MutableList<Uri>>()
    val joinedClubs = MutableLiveData<List<Club>>()

    val currentlySelectedClub = MutableLiveData<Club>()

    init {
        getJoinedClubs()
    }

    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }
    fun updateEventName(newVal: TextFieldValue) {
        eventName.value = newVal
    }
    fun updateSelectedClub(newVal: String) {
        selectedClub.value = newVal
        getSelectedClub(newVal)
    }
    fun getSelectedClub(clubId: String) {
        firebase.getClub(uid = clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { currentlySelectedClub.postValue(fetchedClub) }
            }
            .addOnFailureListener {
                Log.e("FetchClub", "getClubFail: ", it)
            }
    }
    fun updateEventPrivacySelection(leftVal: Boolean, rightVal: Boolean) {
        leftSelected.value = leftVal
        rightSelected.value = rightVal
        eventIsPrivate.value = rightVal
    }
    fun updateSelectedDate(years: Int, month: Int, day: Int, hour: Int, minutes: Int) {
        selectedDate.value = Date(years, month, day, hour,minutes)
        Log.d("dateSelection", selectedDate.value.toString())
    }
    fun updateEventDescription(newVal: TextFieldValue) {
        eventDescription.value = newVal
    }
    fun updateEventParticipantLimit(newVal: TextFieldValue) {
        eventParticipantLimit.value = newVal
    }
    fun updateEventLocation(newVal: TextFieldValue) {
        eventLocation.value = newVal
    }
    fun updateContactInfoName(newVal: TextFieldValue) {
        contactInfoName.value = newVal
    }
    fun updateContactInfoEmail(newVal: TextFieldValue) {
        contactInfoEmail.value = newVal
    }
    fun updateContactInfoNumber(newVal: TextFieldValue) {
        contactInfoNumber.value = newVal
    }
    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }
    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    private fun getJoinedClubs() {
        firebase.uid?.let {
            firebase.getAllClubs().whereArrayContains("members", it).get()
                .addOnSuccessListener { club ->
                    joinedClubs.value = club.toObjects(Club::class.java)
                }
        }
    }

    val givenLinksLiveData = MutableLiveData<Map<String, String>>(mapOf())

    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap

        }
    }

    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    fun addEvent(event: Event) : String {
        return firebase.addEvent(event)
    }

    fun addNewEventNotif(event: Event) {
        FirebaseHelper.addNewEventNotif(eventId = event.id, clubId = event.clubId)
    }

    private var count = 0
    fun storeImagesOnFirebase(listToStore: List<Uri>, eventId: String) {
        listToStore.forEach { uri ->
            firebase.addPic(uri ,"${CollectionName.events}/$eventId/$count.jpg")
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

    fun temporarilyStoreImages(images: MutableList<Uri>) {
        selectedImages.value = images
    }

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

    fun quickFillOptions(user: User) {
        contactInfoName.value = TextFieldValue("${user.fName} ${user.lName}")
        contactInfoEmail.value = TextFieldValue(user.email)
        contactInfoNumber.value = TextFieldValue(user.phone)
    }

}