package com.example.hobbyclubs.api

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
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

    // Event 
    fun addEvent(event: Event) : String {
        val ref = db.collection(CollectionName.events).document()
        val eventWithId = event.apply {
            id = ref.id
        }
        ref.set(eventWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addEvent: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addEvent: ", e)
            }
        return ref.id
    }

    fun getAllEvents() = db.collection(CollectionName.events)

    fun sendEventImage(imageId: String, eventId: String, imageBitmap: Bitmap) {
        val storageRef = Firebase.storage.reference.child("events").child(eventId).child(imageId)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                Log.d(TAG, "sendImage: picture uploaded ($imageId)")
            }
    }

    // News
    
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

    // User

    fun addUser(user: User) {
        val ref = db.collection(CollectionName.users)
        ref.add(user)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: $ref")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "addUser: ", e)
            }
    }

    fun getCurrentUser() : DocumentReference {
        return db.collection(CollectionName.users).document(uid!!)
    }

    fun getAllUsers() = db.collection(CollectionName.users)

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
        const val users = "users"
    }
}

data class User(
    val uid: String = "",
    val fname: String = "",
    val lname: String = "",
    val phone: String = "",
    val email: String = "",
): Serializable

data class Club(
    val name: String
): Serializable

data class Event(
    var id: String = "",
    val clubId: String = "",
    val name: String = "",
    val description: String = "",
    val date: String = "",
    val address: String = "",
    val participantLimit: Int = -1,
    val linkArray: MutableList<Pair<String, String>> = mutableListOf(),
    val contactInfoName: String = "",
    val contactInfoEmail: String = "",
    val contactInfoNumber: String = "",
): Serializable

data class News(
    val name: String,
    val date: Timestamp
): Serializable