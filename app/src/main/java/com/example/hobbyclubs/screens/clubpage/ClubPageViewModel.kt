package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()
    val listOfEvents = MutableLiveData<List<Event>>(listOf())

    val currentUser = MutableLiveData<User>()

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

    fun joinEvent(eventId: String, user: User) {
        firebase.addUserToEvent(eventId = eventId, user = user)
    }
}