package com.example.hobbyclubs.screens.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel() {
    companion object {
        const val TAG = "HomeScreenViewModel"
    }

    val myClubs = MutableLiveData<List<Club>>()
    val upcomingEvents = MutableLiveData<List<Event>>()
    val joinedEvents = MutableLiveData<List<Event>>()
    val likedEvents = MutableLiveData<List<Event>>()
    val news = MutableLiveData<List<News>>()

    val isRefreshing = MutableLiveData(false)

    init {
        refresh()
    }

    fun refresh() {
        isRefreshing.value = true
        viewModelScope.launch {
            fetchMyClubs()
            fetchMyEvents()
            delay(1500)
            isRefreshing.postValue(false)
        }
    }

    fun fetchMyClubs() {
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getAllClubs().whereArrayContains("members", uid)
                .get()
                .addOnSuccessListener { data ->
                    val fetchedClubs =
                        data.toObjects(Club::class.java).sortedBy { club -> club.nextEvent }
                    myClubs.value = fetchedClubs
                    fetchMyNews(fetchedClubs)
                }
                .addOnFailureListener {
                    Log.e(TAG, "fetchMyClubs: ", it)
                }
        }
    }

    fun fetchMyEvents() {
        val now = Timestamp.now()
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getAllEvents().whereArrayContains("participants", uid)
                .addSnapshotListener { data, error ->
                    data ?: run {
                        Log.e(TAG, "fetchMyEvents: ", error)
                        return@addSnapshotListener
                    }
                    val joined = data.toObjects(Event::class.java)
                        .filter { it.date >= now }
                    joinedEvents.value = joined
                }
            FirebaseHelper.getAllEvents().whereArrayContains("likers", uid)
                .addSnapshotListener { data, error ->
                    data ?: run {
                        Log.e(TAG, "fetchMyEvents: ", error)
                        return@addSnapshotListener
                    }
                    val liked = data.toObjects(Event::class.java)
                        .filter { it.date >= now }
                    likedEvents.value = liked
                }
        }
    }

    fun fetchMyNews(myClubs: List<Club>) {
        val now = Timestamp.now()
        FirebaseHelper.getAllNews()
            .get()
            .addOnSuccessListener {
                val fetchedNews = it.toObjects(News::class.java)
                    .filter { news -> myClubs.map { it.ref }.contains(news.clubId) }
                    .filter { news -> news.date >= now }
                    .sortedBy { news -> news.date }
                news.value = fetchedNews
            }
            .addOnFailureListener {
                Log.e(TAG, "fetchMyNews: ", it)
            }
    }

    fun likeEvent(initialLikers: List<String>, eventId: String) {
        FirebaseHelper.uid?.let { uid ->
            val liked = initialLikers.contains(uid)
            val newLikers = if (liked) {
                initialLikers.filter { it != uid }
            } else {
                initialLikers + listOf(uid)
            }
            FirebaseHelper.updateLikeEvent(updatedLikers = newLikers, eventId = eventId)
        }
    }

    fun getBanner(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/banner")

    fun getNextEvent(clubId: String) = FirebaseHelper.getNextEvent(clubId)

    fun getNews(clubId: String) = FirebaseHelper.getAllNewsOfClub(clubId)
}