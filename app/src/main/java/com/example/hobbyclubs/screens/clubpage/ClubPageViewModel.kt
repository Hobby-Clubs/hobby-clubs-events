package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val selectedEvent = MutableLiveData<Event>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()
    val hasJoinedClub = MutableLiveData<Boolean>()
    val isAdmin = MutableLiveData<Boolean>()
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val joinClubDialogText = MutableLiveData<TextFieldValue>()

    val currentUser = MutableLiveData<User>()

    fun updateDialogText(newVal: TextFieldValue) {
        joinClubDialogText.value = newVal
    }

    fun getClub(clubId: String) {
        firebase.getClub(uid = clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
            }
            .addOnFailureListener {
                Log.e("FetchClub", "getClubFail: ", it)
            }
    }

    fun getLogo(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/logo").downloadUrl
            .addOnSuccessListener { logoUri.value = it }

    fun getBanner(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/banner").downloadUrl
            .addOnSuccessListener { bannerUri.value = it }

//    val eventUri = MutableLiveData<MutableMap<String, Uri>>(mutableMapOf())
//    fun getEventImages(eventId: String) {
//        FirebaseHelper.getFile("${CollectionName.events}/${eventId}/0.jpg").downloadUrl
//            .addOnSuccessListener {
//                eventUri.value?.put(eventId, it)
//            }
//    }



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

    fun getClubEvents(clubId: String) {
        firebase.getAllEventsOfClub(clubId).get()
            .addOnSuccessListener { data ->
                val fetchedEvents = data.toObjects(Event::class.java)
                fetchedEvents.let { listOfEvents.postValue(it) }
            }
    }

//    fun joinEvent(eventId: String) {
//        val updatedList =
//
//        firebase.addUserToEvent(eventId = eventId, updatedList)
//    }

    fun getEvent(eventId: String) {
        firebase.getEvent(eventId).get()
            .addOnSuccessListener {
                val fetchedEvent = it.toObject(Event::class.java)
                fetchedEvent?.let { event ->
                    Log.d("getEvent", "event: $event")
                    selectedEvent.postValue(event)
                }
            }
            .addOnFailureListener {
                Log.e("getEvent", "getEventFail: ", it)
            }
    }

    fun joinClub(clubId: String) {
        val updatedList = selectedClub.value?.members?.toMutableList()
        currentUser.value?.let {
            updatedList?.add(it.uid)
        }
        firebase.updateUserInClub(clubId = clubId, updatedList!!)
        getClub(clubId)
    }

    fun leaveClub(clubId: String) {
        val updatedList = selectedClub.value?.members?.toMutableList()
        currentUser.value?.let {
            updatedList?.remove(it.uid)
        }
        firebase.updateUserInClub(clubId = clubId, updatedList!!)
        getClub(clubId)
    }

    fun checkIfUserIsInClub() {
        hasJoinedClub.value = selectedClub.value?.members?.contains(currentUser.value!!.uid)
    }

    fun checkIfUserIsAdmin() {
        isAdmin.value = selectedClub.value?.admins?.contains(currentUser.value!!.uid)
    }
}