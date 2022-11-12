package com.example.hobbyclubs.api

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
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

    fun getUser(uid: String): DocumentReference {
        return db.collection("users").document(uid)
    }

    fun getCurrentUser(): DocumentReference {
        return db.collection("users").document(uid.toString())
    }

    fun addClub(club: Club, logoUri: Uri, bannerUri: Uri) {
        val ref = db.collection(CollectionName.clubs).document()
        val clubWithRef = club.apply { this.ref = ref.id }
        ref.set(clubWithRef)
            .addOnSuccessListener {
                addPic(logoUri, "${CollectionName.clubs}/${ref.id}/logo")
                addPic(bannerUri, "${CollectionName.clubs}/${ref.id}/banner")
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

    fun getFile(path: String): StorageReference {
        return storage.reference.child(path)
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

class ClubCategory {
    companion object {
        const val sports = "sports"
        const val boardGames = "board games"
        const val videoGames = "video games"
        const val music = "music"
        const val movies = "movies"
        const val other = "other"
    }
}

data class User(
    var uid: String = "",
    val fName: String = "",
    val lName: String = "",
    val phone: String = "",
    val email: String = "",
    val interests: List<String> = listOf()
): Serializable

data class Club(
    var ref: String = "0",
    val name: String = "Club name",
    val description: String = "Some cool club" ,
    val admins: List<String> = listOf(),
    val members: List<String> = listOf(),
    val contactPerson: String = "Mikko Mäkelä",
    val contactPhone: String = "050 554 9826",
    val contactEmail: String = "mikko.makela70@nokia.fi",
    val socials: Map<String, String> = mapOf(Pair("Facebook", "https://www.facebook.com")),
    val isPrivate: Boolean = false,
    val created: Timestamp = Timestamp.now(),
    val category: String = ClubCategory.other
): Serializable

data class Event(
    val name: String,
    val date: Timestamp
): Serializable

data class News(
    val name: String,
    val date: Timestamp
): Serializable