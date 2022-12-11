package com.example.hobbyclubs.screens.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.notifications.InAppNotificationService
import com.google.firebase.Timestamp

/**
 * View model containing the logic behind the HomeScreen
 */
class HomeScreenViewModel : ViewModel() {
    companion object {
        const val TAG = "HomeScreenViewModel"
    }

    // Receiver for in app notifications. To show a red dot on the NotificationButton
    private var unreadReceiver: BroadcastReceiver? = null
    val unreadAmount = MutableLiveData(0)
    val isFirstTimeUser = MutableLiveData<Boolean>()
    val allClubs = MutableLiveData<List<Club>>()
    val myClubs = Transformations.map(allClubs) { clubs ->
        val now = Timestamp.now()
        val mClubs = clubs.filter { club -> club.members.contains(FirebaseHelper.uid) }
        val firstOnes = mClubs
            .filter { club -> club.nextEvent != null }
            .filter { club -> club.nextEvent!! >= now }
            .sortedBy { club -> club.nextEvent }

        firstOnes + mClubs.filter { club -> !firstOnes.contains(club) }
    }
    val allEvents = MutableLiveData<List<Event>>()
    val myEvents = Transformations.map(allEvents) { events ->
        events
            .filter { event ->
                event.participants.contains(FirebaseHelper.uid)
                        || event.likers.contains(FirebaseHelper.uid)
                        || event.admins.contains(FirebaseHelper.uid)
            }
            .sortedBy { it.date }
    }

    val allNews = MutableLiveData<List<News>>()
    val myNews = Transformations.map(allNews) { news ->
        myClubs.value?.let { clubList ->
            news.filter { singleNews ->
                clubList.map { club -> club.ref }.contains(singleNews.clubId)
            }
        } ?: listOf()
    }

    val searchInput = MutableLiveData("")

    // On start, checks if the user opens the app for the first time then fetches all necessary data
    init {
        checkFirstTime()
        fetchAllClubs()
        fetchAllEvents()
        fetchAllNews()
    }

    /**
     * Fetches the value of firstTime on the current user's firestore document
     *
     */
    private fun checkFirstTime() {
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

    /**
     * Sets a snapshot listener which will fetch all upcoming events on the app
     *
     */
    private fun fetchAllEvents() {
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

    /**
     * Sets a snapshot listener which will fetch all clubs on the app
     *
     */
    private fun fetchAllClubs() {
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

    /**
     * Sets a snapshot listener which will fetch all news on the app
     *
     */
    private fun fetchAllNews() {
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

    /**
     * When typing in the search bar, updates the search query to show relevant results
     *
     * @param newVal
     */
    fun updateInput(newVal: String) {
        searchInput.value = newVal
    }


    /**
     * Sets a broadcast receiver for unread in app notifications.
     * If there are unread notifications, the a red dot is shown on the NotificationButton of the
     * HomeScreen
     *
     * @param context
     */
    fun receiveUnreads(context: Context) {
        val unreadFilter = IntentFilter()
        unreadFilter.addAction(InAppNotificationService.NOTIF_UNREAD)
        unreadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val unreads =
                    p1?.getParcelableArrayExtra(InAppNotificationService.EXTRA_NOTIF_UNREAD)
                        ?.map { it as NotificationInfo }
                        ?.filter { !it.readBy.contains(FirebaseHelper.uid) }
                        ?: listOf()
                unreadAmount.value = unreads.size
            }
        }
        context.registerReceiver(unreadReceiver, unreadFilter)
    }

    /**
     * Unregisters the receiver for unread in app notifications
     *
     * @param context
     */
    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(unreadReceiver)
    }
}