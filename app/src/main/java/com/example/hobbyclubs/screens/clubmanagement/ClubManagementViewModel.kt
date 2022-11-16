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
    val isPrivate = Transformations.map(selectedClub) {
        it.isPrivate
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())

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
        firebase.getAllEventsOfClub(clubId).get()
            .addOnSuccessListener { data ->
                val fetchedEvents = data.toObjects(Event::class.java)
                fetchedEvents.let { listOfEvents.postValue(it) }
            }
    }

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
}