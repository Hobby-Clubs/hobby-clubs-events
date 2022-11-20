package com.example.hobbyclubs.screens.clubmanagement

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.firestore.Query

class ClubManagementViewModel() : ViewModel() {

    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val selectedNewsId = MutableLiveData<String>()
    val selectedEventId = MutableLiveData<String>()
    val isPrivate = Transformations.map(selectedClub) {
        it.isPrivate
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val listOfRequests = MutableLiveData<List<Request>>()

    fun updateSelection(newsId: String? = null, eventId: String? = null) {
        newsId?.let { selectedNewsId.value = it }
        eventId?.let { selectedEventId.value = it }
    }

    fun updatePrivacy(clubIsPrivate: Boolean, clubId: String) {
        getClub(clubId)
        selectedClub.let {
            firebase.updatePrivacy(clubId = it.value!!.ref, newValue = clubIsPrivate)
        }
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

    fun getClubEvents(clubId: String) {
        firebase.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllEvents", "EventFetchFail: ", error)
                    return@addSnapshotListener
                }
//                Log.d("fetchEvents", "getClubEvents: ${data.documents[0].get("date")}")
                val fetchedEvents = data.toObjects(Event::class.java)
//                Log.d("fetchEvents", fetchedEvents.toString())
                listOfEvents.value = fetchedEvents.filter { it.clubId == clubId }
            }
    }

    fun getAllNews(clubId: String) {
        firebase.getAllNews()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllNews", "NewsFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                Log.d("fetchNews", fetchedNews.toString())
                listOfNews.value = fetchedNews.filter { it.clubId == clubId }.sortedBy { it.date }
            }
    }

    fun deleteNews() {
        selectedNewsId.value?.let { id ->
            firebase.deleteNews(id)
        }
    }
    fun deleteEvent() {
        selectedEventId.value?.let { id ->
            firebase.deleteEvent(id)
        }
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
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}