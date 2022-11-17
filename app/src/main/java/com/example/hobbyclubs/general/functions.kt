package com.example.hobbyclubs.general

import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper

fun joinEvent(event: Event) {
    val updatedList = event.participants.toMutableList()
    FirebaseHelper.uid?.let {
        updatedList.add(it)
    }
    FirebaseHelper.addUserToEvent(eventId = event.id, updatedList)
}

fun likeEvent(event: Event) {
    val initialLikers = event.likers
    FirebaseHelper.uid?.let { uid ->
        val liked = initialLikers.contains(uid)
        val newLikers = if (liked) {
            initialLikers.filter { it != uid }
        } else {
            initialLikers + listOf(uid)
        }
        FirebaseHelper.updateLikeEvent(updatedLikers = newLikers, eventId = event.id)
    }
}

