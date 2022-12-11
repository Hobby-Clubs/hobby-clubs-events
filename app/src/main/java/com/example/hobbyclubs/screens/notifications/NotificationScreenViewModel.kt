package com.example.hobbyclubs.screens.notifications

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.NotificationInfo
import com.example.hobbyclubs.api.NotificationType.*
import com.example.hobbyclubs.notifications.InAppNotificationHelper
import com.example.hobbyclubs.notifications.InAppNotificationService
import com.example.hobbyclubs.notifications.NotificationContent
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationScreenViewModel(application: Application) : AndroidViewModel(application) {
    private var unreadReceiver: BroadcastReceiver? = null
    private val helper = InAppNotificationHelper(application)
    val unreads = MutableLiveData<List<NotificationContent>>()
    val isRefreshing = MutableLiveData(false)
    private val isPausedPref = application.getSharedPreferences("paused", Context.MODE_PRIVATE)

    init {
        refresh()
    }

    /**
     * Fetches the in app notifications again
     *
     */
    fun refresh() {
        isRefreshing.value = true
        fetchUnreads()
    }

    /**
     * Sets up a broadcast receiver for in app notifications.
     * The broadcasts come from the [InAppNotificationService]
     *
     * @param context
     */
    fun receiveUnreads(context: Context) {
        val unreadFilter = IntentFilter()
        unreadFilter.addAction(InAppNotificationService.NOTIF_UNREAD)
        unreadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                val infos = p1?.getParcelableArrayExtra(
                    InAppNotificationService.EXTRA_NOTIF_UNREAD,
                )?.toList() ?: listOf()
                convertToContents(
                    infos.map { it as NotificationInfo }
                        .filter { !it.readBy.contains(FirebaseHelper.uid) }
                )
            }
        }
        context.registerReceiver(unreadReceiver, unreadFilter)
    }

    /**
     * Converts a received list of [NotificationInfo] into a list of [NotificationContent]
     * which contain all the content to display in the list of in app notifications
     *
     * @param notifInfos
     */
    fun convertToContents(notifInfos: List<NotificationInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val contents = notifInfos.map {
                when (valueOf(it.type)) {
                    EVENT_CREATED -> withContext(coroutineContext) {
                        helper.newEventToContent(it)
                    }
                    NEWS_CLUB -> withContext(coroutineContext) {
                        helper.clubNewsToContent(it)
                    }
                    NEWS_GENERAL -> withContext(coroutineContext) {
                        helper.generalNewsToContent(it)
                    }
                    CLUB_REQUEST_PENDING -> withContext(coroutineContext) {
                        helper.clubRequestToContent(it)
                    }
                    CLUB_REQUEST_ACCEPTED -> withContext(coroutineContext) {
                        helper.clubAcceptedRequestToContent(it)
                    }
                    EVENT_REQUEST_PENDING -> withContext(coroutineContext) {
                        helper.eventRequestToContent(it)
                    }
                    EVENT_REQUEST_ACCEPTED -> withContext(coroutineContext) {
                        helper.eventAcceptedRequestToContent(it)
                    }
                }
            }
            unreads.postValue(contents.filterNotNull())
        }
    }

    /**
     * Fetches unread in app notifications
     *
     */
    private fun fetchUnreads() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = helper.getMyNotifsContents()
            unreads.postValue(list)
            isRefreshing.postValue(false)
        }
    }

    /**
     * Unregisters receiver for in app notifications from [InAppNotificationService]
     *
     * @param context
     */
    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(unreadReceiver)
    }

    /**
     * Marks a notification as read by the current user by adding their user id to the readBy array
     * of the corresponding [NotificationInfo] on firestore.
     *
     * @param id
     */
    fun markAsRead(id: String) {
        unreads.value = unreads.value?.filter { it.id != id }
        FirebaseHelper.markNotificationAsSeen(id)
    }

    /**
     * For demo purposes, removes the current user's id from all readBy arrays in [NotificationInfo]
     * to undo the dismissal of all in app notifications.
     *
     */
    fun removeRead() {
        setIsPaused(true)
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getNotifications().get()
                .addOnSuccessListener {
                    val notifs = it.toObjects(NotificationInfo::class.java)
                    viewModelScope.launch {
                        notifs.forEach { notif ->
                            withContext(Dispatchers.IO) {
                                FirebaseHelper.getNotifications().document(notif.id)
                                    .update("readBy", FieldValue.arrayRemove(uid))
                                    .await()
                            }
                        }
                        setIsPaused(false)
                        fetchUnreads()
                    }
                }
        }
    }

    /**
     * Pauses the fetching of in app notifications by the [InAppNotificationService]
     *
     * @param isPaused
     */
    private fun setIsPaused(isPaused: Boolean) {
        isPausedPref.edit().apply {
            putBoolean("isPaused", isPaused)
            apply()
        }
    }

    /**
     * Dismisses all in app notifications by marking them as read.
     * Pauses the fetching of in app notifications while doing so and refreshes
     * the list when done
     *
     * @param contents the current list of [NotificationContent]
     */
    fun markAllAsRead(contents: List<NotificationContent>) {
        setIsPaused(true)
        contents.map { it.id }.forEach {
            FirebaseHelper.markNotificationAsSeen(it)
        }
        unreads.value = listOf()
        setIsPaused(false)
        refresh()
    }
}