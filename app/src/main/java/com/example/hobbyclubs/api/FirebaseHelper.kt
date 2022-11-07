package com.example.hobbyclubs.api

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.lang.Exception

object FirebaseHelper {
    const val TAG = "FirebaseHelper"
    val db get() = Firebase.firestore
    val uid get() = Firebase.auth.uid

    fun addClub(club: Club) {
        val ref = db.collection(CollectionName.clubs)
        ref.add(club)
            .addOnSuccessListener {
                Log.d(TAG, "addClub: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addClub: ", e)
            }
    }

    fun getAllClubs() = db.collection(CollectionName.clubs)

    fun addEvent(event: Event) {
        val ref = db.collection(CollectionName.events)
        ref.add(event)
            .addOnSuccessListener {
                Log.d(TAG, "addEvent: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addEvent: ", e)
            }
    }

    fun getAllEvents() = db.collection(CollectionName.events)

    fun addNews(news: News) {
        val ref = db.collection(CollectionName.news)
        ref.add(news)
            .addOnSuccessListener {
                Log.d(TAG, "addNews: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addNews: ", e)
            }
    }

    fun getAllNews() = db.collection(CollectionName.news)
}

class CollectionName {
    companion object {
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
    }
}

data class Club(
    val name: String
): Serializable

data class Event(
    val name: String,
    val date: Timestamp
): Serializable

data class News(
    val name: String,
    val date: Timestamp
): Serializable