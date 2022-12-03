package com.example.hobbyclubs.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.notifications.InAppNotificationService
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeScreenViewModel : ViewModel() {
    companion object {
        const val TAG = "HomeScreenViewModel"
    }

    var unreadReceiver: BroadcastReceiver? = null
    val unreadAmount = MutableLiveData<Int>(0)
    val userPicUri = MutableLiveData<Uri>()
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
        getUserPicUri()
        fetchAllClubs()
        fetchAllEvents()
        fetchAllNews()
    }

    fun getUserPicUri() {
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getFile("${CollectionName.users}/$uid")
                .downloadUrl
                .addOnSuccessListener {
                    userPicUri.value = it
                }
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

    fun receiveUnreads(context: Context) {
        val unreadFilter = IntentFilter()
        unreadFilter.addAction(InAppNotificationService.NOTIF_UNREAD)
        unreadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(TAG, "onReceive: unreads")
                val unreads = p1?.getParcelableArrayExtra(InAppNotificationService.EXTRA_NOTIF_UNREAD,)
                    ?.map { it as NotificationInfo }
                    ?.filter { !it.readBy.contains(FirebaseHelper.uid) }
                    ?: listOf()
                unreadAmount.value = unreads.size
            }
        }
        context.registerReceiver(unreadReceiver, unreadFilter)
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(unreadReceiver)
    }
}