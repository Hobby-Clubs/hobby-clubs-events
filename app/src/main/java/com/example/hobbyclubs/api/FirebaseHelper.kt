package com.example.hobbyclubs.api

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
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

    fun getUser(uid: String): DocumentReference {
        return db.collection("users").document(uid)
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

    fun getClub(uid: String) : DocumentReference {
        return db.collection(CollectionName.clubs).document(uid)
    }

    fun getAllClubs() = db.collection(CollectionName.clubs)

    fun updateClubNextEvent(clubId: String, date: Timestamp) {
        val ref = db.collection(CollectionName.clubs).document(clubId)
        ref.update("nextEvent", date)
            .addOnSuccessListener {
                Log.d(TAG, "updateClubNextEvent: $clubId next event updated")
            }
            .addOnFailureListener {
                Log.e(TAG, "updateClubNextEvent: ", it)
            }

    }

    fun addEvent(event: Event): String {
        val ref = db.collection(CollectionName.events).document()
        val eventWithId = event.apply {
            id = ref.id
        }
        ref.set(eventWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addEvent: $ref")
                event.clubId?.let { id ->
                    getNextEvent(id)
                        .get()
                        .addOnSuccessListener {
                            val next = it.toObjects(Event::class.java)[0]
                            updateClubNextEvent(id, next.date)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addEvent: ", e)
            }
        return ref.id
    }

    fun getAllEvents() = db.collection(CollectionName.events)

    fun sendEventImage(imageId: String, eventId: String, imageBitmap: Bitmap) {
        val storageRef =
            Firebase.storage.reference.child("events").child(eventId).child(imageId)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                Log.d(TAG, "sendImage: picture uploaded ($imageId)")
            }
    }

    // News

    fun getAllEventsOfClub(clubId: String): Query {
        return db.collection(CollectionName.events).whereEqualTo("clubId", clubId)
    }

    fun getNextEvent(clubId: String): Query {
        return getAllEventsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING).limit(1L)
    }

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

    fun getAllNewsOfClub(clubId: String) = getAllNews().whereEqualTo("clubId", clubId)

    // User

    fun getCurrentUser(): DocumentReference {
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
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
        const val users = "users"
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
) : Serializable

data class Club(
    var ref: String = "0",
    val name: String = "Club name",
    val description: String = "Some cool club",
    val admins: List<String> = listOf(),
    val members: List<String> = listOf(),
    val contactPerson: String = "Mikko Mäkelä",
    val contactPhone: String = "050 554 9826",
    val contactEmail: String = "mikko.makela70@nokia.fi",
    val socials: Map<String, String> = mapOf(Pair("Facebook", "https://www.facebook.com")),
    val isPrivate: Boolean = false,
    val created: Timestamp = Timestamp.now(),
    val category: String = ClubCategory.other,
    val nextEvent: Timestamp? = null
) : Serializable

data class Event(
    var id: String = "",
    val clubId: String = "",
    val name: String = "",
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val address: String = "",
    val participantLimit: Int = -1,
    val linkArray: Map<String, String> = mapOf(),
    val contactInfoName: String = "",
    val contactInfoEmail: String = "",
    val contactInfoNumber: String = "",
) : Serializable

data class News(
    val clubId: String? = null,
    val name: String,
    val date: Timestamp
) : Serializable