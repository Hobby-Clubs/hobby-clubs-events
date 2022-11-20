package com.example.hobbyclubs.screens.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel() {
    companion object {
        const val TAG = "HomeScreenViewModel"
    }

    val isFirstTimeUser = MutableLiveData<Boolean>()
    val allClubs = MutableLiveData<List<Club>>()
    val allEvents = MutableLiveData<List<Event>>()
    val myClubs = MutableLiveData<List<Club>>()
    val upcomingEvents = MutableLiveData<List<Event>>()
    val joinedEvents = MutableLiveData<List<Event>>()
    val likedEvents = MutableLiveData<List<Event>>()
    val clubEvents = MutableLiveData<List<Event>>()
    val news = MutableLiveData<List<News>>()
    val isRefreshing = MutableLiveData(false)
    val searchInput = MutableLiveData("")

    init {
        refresh()
    }

    fun refresh() {
        checkFirstTime()
        isRefreshing.value = true
        viewModelScope.launch {
            fetchMyClubs()
            fetchMyEvents()
            delay(1500)
            isRefreshing.postValue(false)
        }
    }

    fun checkFirstTime() {
        FirebaseHelper.uid?.let {
            FirebaseHelper.getUser(it)
                .get()
                .addOnSuccessListener { data ->
                    val currentUser = data.toObject(User::class.java)
                    if (currentUser != null) {
                        isFirstTimeUser.value = currentUser.firstTime
                    }
                }
        }
    }

    fun fetchEventsOfMyClubs(myClubs: List<Club>) {
        FirebaseHelper.getAllEvents()
            .get()
            .addOnSuccessListener {
                val events = it.toObjects(Event::class.java)
                if (events.isEmpty()) {
                    return@addOnSuccessListener
                }
                val filtered = events.filter { event ->
                    myClubs.map { club -> club.ref }.contains(event.clubId)
                }
                clubEvents.value = filtered
            }
    }

    fun fetchAllClubs() {
        FirebaseHelper.getAllClubs()
            .get()
            .addOnSuccessListener {
                val clubs = it.toObjects(Club::class.java)
                if (clubs.isEmpty()) {
                    return@addOnSuccessListener
                }
                allClubs.value = clubs.sortedByDescending { club -> club.members.size }
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
        FirebaseHelper.getAllNews()
            .get()
            .addOnSuccessListener {
                val fetchedNews = it.toObjects(News::class.java)
                    .filter { news -> myClubs.map { it.ref }.contains(news.clubId) }
                    .sortedBy { news -> news.date }
                news.value = fetchedNews
            }
            .addOnFailureListener {
                Log.e(TAG, "fetchMyNews: ", it)
            }
    }

    fun getLogo(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/logo")
    fun getBanner(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/banner")

    fun getNextEvent(clubId: String) = FirebaseHelper.getNextEvent(clubId)

    fun getNews(clubId: String) = FirebaseHelper.getAllNewsOfClub(clubId)

    fun updateInput(newVal: String) {
        searchInput.value = newVal
    }

}