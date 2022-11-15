package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()
    val hasJoinedClub = Transformations.map(selectedClub) {
        it.members.contains(firebase.uid)
    }
    val isAdmin = Transformations.map(selectedClub) {
        it.admins.contains(firebase.uid)
    }
    val clubIsPrivate = Transformations.map(selectedClub) {
        it.isPrivate
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val joinClubDialogText = MutableLiveData<TextFieldValue>()

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

    fun getEventBackground(eventId: String) =
        FirebaseHelper.getFile("${CollectionName.events}/$eventId/0.jpg")

    fun getAllNews(clubId: String) {
        firebase.getAllNewsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { data ->
                val fetchedNews = data.toObjects(News::class.java)
                Log.d("fetchNews", fetchedNews.toString())
                fetchedNews.let { listOfNews.postValue(it) }
            }
            .addOnFailureListener { e ->
                Log.e("fetchNews", "fetchNewsFail: ", e)
            }
    }

    fun getClubEvents(clubId: String) {
        firebase.getAllEventsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { data ->
                val fetchedEvents = data.toObjects(Event::class.java)
                fetchedEvents.let { listOfEvents.postValue(it) }
            }
    }

    fun joinEvent(event: Event) {
        val updatedList = event.participants.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserToEvent(eventId = event.id, updatedList)
        getClubEvents(event.clubId)
    }

    fun likeEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.add(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
        getClubEvents(event.clubId)
    }

    fun removeLikeOnEvent(event: Event) {
        val updatedList = event.likers.toMutableList()
        firebase.uid?.let {
            updatedList.remove(it)
        }
        firebase.addUserLikeToEvent(eventId = event.id, updatedList)
    }

    fun getEvent(eventId: String) = firebase.getEvent(eventId)

    fun joinClub(clubId: String) {
        val updatedList = selectedClub.value?.members?.toMutableList()
        firebase.uid?.let {
            updatedList?.add(it)
        }
        firebase.updateUserInClub(clubId = clubId, updatedList!!)
        getClub(clubId)
    }

    fun leaveClub(clubId: String) {
        val updatedList = selectedClub.value?.members?.toMutableList()
        firebase.uid?.let {
            updatedList?.remove(it)
        }
        firebase.updateUserInClub(clubId = clubId, updatedList!!)
        getClub(clubId)
    }
}