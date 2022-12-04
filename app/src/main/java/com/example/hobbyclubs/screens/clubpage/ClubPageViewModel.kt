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

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val clubsRequests = MutableLiveData<List<ClubRequest>>()
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
    val eventRequests = MutableLiveData<List<EventRequest>>()
    val hasRequestedToEvent = Transformations.map(eventRequests) { list ->
        list.any { it.userId == FirebaseHelper.uid && !it.acceptedStatus }
    }
    fun getEventJoinRequests(eventId: String) {
        FirebaseHelper.getRequestsFromEvent(eventId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(EventRequest::class.java)
                eventRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val correctList = MutableLiveData<List<News>>(listOf())
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

    fun getAllNews(clubId: String, fromHomeScreen: Boolean = false) {
        firebase.getAllNewsOfClub(clubId).orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllNews", "getAllNews: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                Log.d("fromhomescreen", fromHomeScreen.toString())
                if (fromHomeScreen){
                    listOfNews.value = fetchedNews.filter { !it.usersRead.contains(FirebaseHelper.uid) }
                } else {
                    listOfNews.value = fetchedNews
                }
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
        firebase.uid?.let { uid ->
            firebase.updateUserInClub(clubId = clubId, userId = uid)
            getClub(clubId)
        }
    }

    fun leaveClub(clubId: String) {
        firebase.uid?.let { uid ->
            firebase.updateUserInClub(clubId = clubId, userId = uid, remove = true)
            getClub(clubId)
        }
    }

    fun sendJoinClubRequest(clubId: String, request: ClubRequest) {
        firebase.addClubRequest(clubId = clubId, request = request)
    }

    fun updateUserWithProfilePicUri(userId: String) {
        firebase.getUser(userId).get()
            .addOnSuccessListener {
                val fetchedUser = it.toObject(User::class.java)
                fetchedUser?.let { user ->
                    val changeMap = mapOf(
                        Pair("profilePicUri", user.profilePicUri)
                    )
                    firebase.updateUser(user.uid, changeMap)
                }
            }
    }

    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(ClubRequest::class.java)
                Log.d("fetchNews", fetchedRequests.toString())
                clubsRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

}