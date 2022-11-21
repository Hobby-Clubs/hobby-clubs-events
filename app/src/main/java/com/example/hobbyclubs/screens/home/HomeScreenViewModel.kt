package com.example.hobbyclubs.screens.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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

    val isFirstTimeUser = MutableLiveData<Boolean>()
    val allClubs = MutableLiveData<List<Club>>()
    val myClubs = Transformations.map(allClubs) { clubs ->
        clubs.filter { it.members.contains(FirebaseHelper.uid) }
    }
    val allEvents = MutableLiveData<List<Event>>()
    val myEvents = Transformations.map(allEvents) { events ->
        events
            .filter { event ->
            event.participants.contains(FirebaseHelper.uid)
                    || event.likers.contains(FirebaseHelper.uid)
            }
            .sortedBy { it.date }
    }

    val allNews = MutableLiveData<List<News>>()
    val searchInput = MutableLiveData("")

    init {
        checkFirstTime()
        fetchAllClubs()
        fetchAllEvents()
        fetchAllNews()
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

    fun fetchAllEvents() {
        val now = Timestamp.now()
        FirebaseHelper.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e(TAG, "fetchAllEvents: ", error)
                    return@addSnapshotListener
                }
                val events = data.toObjects(Event::class.java)
                if (events.isEmpty()) {
                    return@addSnapshotListener
                }
                allEvents.value = events.filter { it.date >= now }
            }
    }

    fun fetchAllClubs() {
        FirebaseHelper.getAllClubs()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e(TAG, "fetchAllClubs: ", error)
                    return@addSnapshotListener
                }
                val clubs = data.toObjects(Club::class.java)
                if (clubs.isEmpty()) {
                    return@addSnapshotListener
                }
                allClubs.value = clubs.sortedByDescending { club -> club.members.size }
            }
    }

//    fun fetchMyClubs() {
//        FirebaseHelper.uid?.let { uid ->
//            FirebaseHelper.getAllClubs().whereArrayContains("members", uid)
//                .get()
//                .addOnSuccessListener { data ->
//                    val fetchedClubs =
//                        data.toObjects(Club::class.java).sortedBy { club -> club.nextEvent }
//                    myClubs.value = fetchedClubs
//                    fetchMyNews(fetchedClubs)
//                }
//                .addOnFailureListener {
//                    Log.e(TAG, "fetchMyClubs: ", it)
//                }
//        }
//    }

//    fun fetchMyEvents() {
//        val now = Timestamp.now()
//        FirebaseHelper.uid?.let { uid ->
//            FirebaseHelper.getAllEvents().whereArrayContains("participants", uid)
//                .addSnapshotListener { data, error ->
//                    data ?: run {
//                        Log.e(TAG, "fetchMyEvents: ", error)
//                        return@addSnapshotListener
//                    }
//                    val joined = data.toObjects(Event::class.java)
//                        .filter { it.date >= now }
//                    joinedEvents.value = joined
//                }
//            FirebaseHelper.getAllEvents().whereArrayContains("likers", uid)
//                .addSnapshotListener { data, error ->
//                    data ?: run {
//                        Log.e(TAG, "fetchMyEvents: ", error)
//                        return@addSnapshotListener
//                    }
//                    val liked = data.toObjects(Event::class.java)
//                        .filter { it.date >= now }
//                    likedEvents.value = liked
//                }
//        }
//    }

    fun fetchAllNews() {
        FirebaseHelper.getAllNews()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e(TAG, "fetchAllNews: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                    .sortedByDescending { news -> news.date }
                allNews.value = fetchedNews
            }
    }

    fun getLogo(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/logo")
    fun getBanner(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/banner")
    fun updateInput(newVal: String) {
        searchInput.value = newVal
    }

}