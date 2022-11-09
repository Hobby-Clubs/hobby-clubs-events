package com.example.hobbyclubs.api

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable

object FirebaseHelper {
    const val TAG = "FirebaseHelper"
    private val db get() = Firebase.firestore

    // Firestore

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

    // Auth

    private val auth = Firebase.auth
    val uid get() = auth.uid
    val currentUser = auth.currentUser

    fun login(email: String, pwd: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, pwd)
    }

    fun register(email: String, pwd: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, pwd)
    }

    fun logout() {
        auth.signOut()
    }

}

class CollectionName {
    companion object {
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
    }
}

data class User(
    val name: String,
    val avatarId: Int,
): Serializable

data class Club(
    val name: String
): Serializable

data class Event(
    val clubId: String,
    val name: String,
    val description: String,
    val date: Timestamp,
    val address: String,
    val participantLimit: Int,
    val contactInfoName: String,
    val contactInfoEmail: String,
    val contactInfoNumber: String,
): Serializable

data class News(
    val name: String,
    val date: Timestamp
): Serializable