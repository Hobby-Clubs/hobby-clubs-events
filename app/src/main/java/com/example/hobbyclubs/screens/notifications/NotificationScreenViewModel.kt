package com.example.hobbyclubs.screens.notifications

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.NotificationInfo
import com.example.hobbyclubs.api.NotificationType.*
import com.example.hobbyclubs.notifications.InAppNotificationHelper
import com.example.hobbyclubs.notifications.InAppNotificationService
import com.example.hobbyclubs.notifications.NotificationContent
import com.example.hobbyclubs.screens.home.HomeScreenViewModel
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationScreenViewModel(application: Application): AndroidViewModel(application) {
    private var unreadReceiver: BroadcastReceiver? = null
    val helper = InAppNotificationHelper(application)
    val unreads = MutableLiveData<List<NotificationContent>>()
    val isRefreshing = MutableLiveData(false)

    init {
        refresh()
    }

    fun refresh() {
        isRefreshing.value = true
        fetchUnreads()
    }

    fun receiveUnreads(context: Context) {
        val unreadFilter = IntentFilter()
        unreadFilter.addAction(InAppNotificationService.NOTIF_UNREAD)
        unreadReceiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                Log.d(HomeScreenViewModel.TAG, "onReceive: unreads")
                val infos = p1?.
                getParcelableArrayExtra(
                    InAppNotificationService.EXTRA_NOTIF_UNREAD,
                )?.toList() ?: listOf()
                convertToContents(
                    infos.map { it as NotificationInfo }
                        .filter { !it.readBy.contains(FirebaseHelper.uid)}
                )
            }
        }
        context.registerReceiver(unreadReceiver, unreadFilter)
    }

    fun convertToContents(notifInfos: List<NotificationInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val contents = notifInfos.map {
                val type = valueOf(it.type)
                when (type) {
                    EVENT_CREATED -> withContext(coroutineContext) { helper.newEventToContent(it) }
                    NEWS_CLUB -> withContext(coroutineContext) { helper.clubNewsToContent(it) }
                    NEWS_GENERAL -> withContext(coroutineContext) { helper.generalNewsToContent(it) }
                    REQUEST_PENDING -> withContext(coroutineContext) { helper.requestToContent(it) }
                    REQUEST_ACCEPTED -> withContext(coroutineContext) { helper.acceptedRequestToContent(it) }
                }
            }
            unreads.postValue(contents.filterNotNull())
        }
    }

    fun fetchUnreads() {
        viewModelScope.launch(Dispatchers.IO) {
            val list =  helper.getMyNotifsContents()
            unreads.postValue(list)
            isRefreshing.postValue(false)
        }
    }

    fun unregisterReceiver(context: Context) {
        context.unregisterReceiver(unreadReceiver)
    }

    fun markAsRead(id: String) {
        FirebaseHelper.markNotificationAsSeen(id)
        unreads.value = unreads.value?.filter { it.id != id }
    }

    fun removeRead() {
        FirebaseHelper.uid?.let { uid ->
            FirebaseHelper.getNotifications().get()
                .addOnSuccessListener {
                    val notifs = it.toObjects(NotificationInfo::class.java)
                    notifs.forEach { notif ->
                        FirebaseHelper.getNotifications().document(notif.id)
                            .update("readBy", FieldValue.arrayRemove(uid))
                    }
                }
        }
    }
}