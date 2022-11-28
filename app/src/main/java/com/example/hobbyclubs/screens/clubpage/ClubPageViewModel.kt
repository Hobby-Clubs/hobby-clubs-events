package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val clubsRequests = MutableLiveData<List<Request>>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()
    val hasJoinedClub = Transformations.map(selectedClub) {
        it.members.contains(firebase.uid)
    }
    val hasRequested = Transformations.map(clubsRequests) { list ->
        list.any { it.userId == firebase.uid && !it.acceptedStatus }
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
        firebase.getClub(uid = clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getClub", "getClub: ", error)
                    return@addSnapshotListener
                }
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
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
                listOfNews.value = fetchedNews
            }
            .addOnFailureListener { e ->
                Log.e("fetchNews", "fetchNewsFail: ", e)
            }
    }

    fun getClubEvents(clubId: String) {
        val now = Timestamp.now()
        firebase.getAllEventsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("ClubPageViewModel", "getClubEvents: ", error)
                    return@addSnapshotListener
                }
                val fetchedEvents = data.toObjects(Event::class.java)
                listOfEvents.value = fetchedEvents.filter { it.date >= now }
            }
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

    fun sendJoinClubRequest(clubId: String, request: Request) {
        firebase.addRequest(clubId = clubId, request = request)
    }

    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(Request::class.java)
                Log.d("fetchNews", fetchedRequests.toString())
                clubsRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}