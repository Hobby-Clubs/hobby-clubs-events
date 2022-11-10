package com.example.hobbyclubs.screens.create.event

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import okhttp3.internal.notifyAll
import java.net.URL

class CreateEventViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentCreationProgressPage = MutableLiveData<Int>()
    val eventName = MutableLiveData<TextFieldValue?>()
    val eventDescription = MutableLiveData<TextFieldValue>()
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
    fun updateEventDescription(newVal: TextFieldValue) {
        eventDescription.value = newVal
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

    val givenLinksLiveData = MutableLiveData<MutableList<Pair<String, String>>>()
    val givenLinks = mutableListOf<Pair<String, String>>()

    fun addLinkToList(pair: Pair<String, String>) {
        givenLinks.add(pair)
        givenLinksLiveData.value = givenLinks
        givenLinksLiveData.notifyObserver()
    }

    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    fun addEvent(event: Event) {
        firebase.addEvent(event)
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

    fun temporarilyStoreImages(images: MutableList<Uri>) {
        selectedImages.value = images
    }

    fun emptySelection() {
        selectedImages.value = mutableListOf()
    }

}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}