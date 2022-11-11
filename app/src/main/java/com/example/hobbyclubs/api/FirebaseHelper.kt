package com.example.hobbyclubs.api

import android.graphics.Bitmap
import android.net.Uri
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

    fun addUser(user: User) {
        val ref = db.collection("users").document(user.uid)
        ref.set(user)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addUser: ", e)
            }
    }

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




    fun sendNewsImage(imageId: String, newsId: String, imageBitmap: Bitmap) {
        val storageRef = Firebase.storage.reference.child("news").child(newsId).child(imageId)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                Log.d(TAG, "sendImage: picture uploaded ($imageId)")
            }
    }

    fun addNews(news: News) : String {
        val ref = db.collection(CollectionName.news).document()
        val newsId = news.apply {
            id = ref.id
        }
        ref.set(newsId)
            .addOnSuccessListener {
                Log.d(TAG, "addNews: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addNews: ", e)
            }
        return ref.id
    }

    fun getAllNews() = db.collection(CollectionName.news)

    fun getNews( newsId: String):DocumentReference {
        val ref = db.collection(CollectionName.news)
        return ref.document(newsId)
    }
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


    // Storage

    private val storage = Firebase.storage

    fun addPic(uri: Uri, path: String) {
        storage.reference.child(path).putFile(uri)
            .addOnSuccessListener {
                Log.d(TAG, "addPic: $path")
            }
            .addOnFailureListener {
                Log.e(TAG, "addPic: ", it)
            }
    }

}

class CollectionName {
    companion object {
        const val users = "users"
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
    }
}

data class User(
    var uid: String = "",
    val fName: String = "",
    val lName: String = "",
    val phone: String = "",
    val email: String = "",
): Serializable

data class Club(
    val name: String
): Serializable

data class Event(
    val name: String,
    val date: Timestamp
): Serializable

data class News(
    var id: String = "",
    val clubId: String = "",
    val headline: String = "",
    val newsContent: String = "",
    val date: Timestamp = Timestamp(0L,0)
): Serializable