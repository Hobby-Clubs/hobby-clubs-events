package com.example.hobbyclubs.screens.create.event

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import com.google.firebase.Timestamp

class CreateEventViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentUser = MutableLiveData<User>()
    val currentCreationProgressPage = MutableLiveData<Int>()
    val selectedClub = MutableLiveData<String>()
    val selectedDate = MutableLiveData<Timestamp>()
    val eventName = MutableLiveData<TextFieldValue?>()
    val eventDescription = MutableLiveData<TextFieldValue>()
    val eventLocation = MutableLiveData<TextFieldValue>()
    val eventParticipantLimit = MutableLiveData<TextFieldValue>()
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()

    val selectedImages = MutableLiveData<MutableList<Uri>>()

    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }
    fun updateEventName(newVal: TextFieldValue) {
        eventName.value = newVal
    }
    fun updateSelectedClub(newVal: String) {
        selectedClub.value = newVal
    }
    fun updateSelectedDate(newVal: Timestamp) {
        selectedDate.value = newVal
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

    val imagesAsBitmap = MutableLiveData<MutableList<Bitmap>>()
    private val selectedImagesAsBitmaps = mutableListOf<Bitmap>()
    fun convertUriToBitmap(images: List<Uri>, context: Context) {

        images.forEach { image ->
            val source = ImageDecoder.createSource(context.contentResolver, image)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectedImagesAsBitmaps.add(bitmap)
            Log.d("imageList", selectedImagesAsBitmaps.toString())
        }
        imagesAsBitmap.value = selectedImagesAsBitmaps
        imagesAsBitmap.notifyObserver()
    }

    var count = 0
    fun storeBitmapsOnFirebase(listToStore: List<Bitmap>, eventId: String) {
        listToStore.forEach { bitmap ->
            firebase.sendEventImage(imageId = "$count.jpg", eventId = eventId, imageBitmap = bitmap)
            count += 1
        }
    }

    fun temporarilyStoreImages(images: MutableList<Uri>) {
        selectedImages.value = images
    }

    fun emptySelection() {
        selectedImages.value = mutableListOf()
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

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}