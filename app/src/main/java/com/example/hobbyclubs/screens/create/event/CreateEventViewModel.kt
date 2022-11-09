package com.example.hobbyclubs.screens.create.event

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper

class CreateEventViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentCreationProgressPage = MutableLiveData<Int>()
    val eventName = MutableLiveData<TextFieldValue?>()
    val eventDescription = MutableLiveData<TextFieldValue>()
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()

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

    fun addEvent(event: Event) {
        firebase.addEvent(event)
    }
}