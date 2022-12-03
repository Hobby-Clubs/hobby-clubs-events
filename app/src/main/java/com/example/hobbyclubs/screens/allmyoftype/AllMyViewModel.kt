package com.example.hobbyclubs.screens.allmyoftype

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.screens.home.HomeScreenViewModel
import com.google.firebase.Timestamp

class AllMyViewModel : ViewModel() {

    val allClubs = MutableLiveData<List<Club>>()
    val myClubs = Transformations.map(allClubs) { clubs ->
        clubs.filter { club -> club.members.contains(FirebaseHelper.uid) }
    }
    val allEvents = MutableLiveData<List<Event>>()
    val myEvents = Transformations.map(allEvents) { events ->
        events
            .filter { event ->
                (event.participants.contains(FirebaseHelper.uid)
                        || event.likers.contains(FirebaseHelper.uid))
            }
            .sortedBy { it.date }
    }

    val allNews = MutableLiveData<List<News>>()
    val myNews = Transformations.map(allNews) { news ->
        myClubs.value?.let { clubList ->
            news.filter { singleNews ->
                clubList.map { club -> club.ref }.contains(singleNews.clubId)
            }
        }
    }

    fun fetchAllEvents() {
        val now = Timestamp.now()
        FirebaseHelper.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e(HomeScreenViewModel.TAG, "fetchAllEvents: ", error)
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
                    Log.e(HomeScreenViewModel.TAG, "fetchAllClubs: ", error)
                    return@addSnapshotListener
                }
                val clubs = data.toObjects(Club::class.java)
                if (clubs.isEmpty()) {
                    return@addSnapshotListener
                }
                allClubs.value = clubs.sortedByDescending { club -> club.members.size }
            }
    }
    fun fetchAllNews() {
        FirebaseHelper.getAllNews()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e(HomeScreenViewModel.TAG, "fetchAllNews: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                    .sortedByDescending { news -> news.date }
                allNews.value = fetchedNews
            }
    }


}