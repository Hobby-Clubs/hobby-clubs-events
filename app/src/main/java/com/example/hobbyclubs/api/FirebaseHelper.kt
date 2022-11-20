package com.example.hobbyclubs.api

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
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
        return db.collection(CollectionName.users).document(uid)
    }

    fun updateUser(uid: String, changeMap: Map<String, Any>) {
        val ref = db.collection("users").document(uid)
            ref
            .update(changeMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUser: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUser: ", e)
            }
    }

    fun addClub(club: Club): String {
        val ref = db.collection(CollectionName.clubs).document()
        val clubWithRef = club.apply { this.ref = ref.id }
        ref.set(clubWithRef)
            .addOnSuccessListener {
                Log.d(TAG, "addClub: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addClub: ", e)
            }
        return ref.id
    }

    fun getClub(uid: String) : DocumentReference {
        return db.collection(CollectionName.clubs).document(uid)
    }

    fun getAllClubs() = db.collection(CollectionName.clubs)

    fun sendClubImage(imageName: String, clubId: String, imageBitmap: Bitmap) {
        val storageRef =
            Firebase.storage.reference.child("clubs").child(clubId).child(imageName)
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()
        storageRef.putBytes(bytes)
            .addOnSuccessListener {
                Log.d(TAG, "sendImage: picture uploaded ($imageName)")
            }
    }

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

    // Events

    fun addEvent(event: Event): String {
        val ref = db.collection(CollectionName.events).document()
        val eventWithId = event.apply { id = ref.id }
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

    fun deleteEvent(eventId: String) {
        val ref = db.collection(CollectionName.events).document(eventId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteEvent: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteEvent: ", e)
            }
    }

    fun getEvent(eventId: String): DocumentReference {
        return db.collection(CollectionName.events).document(eventId)
    }

    fun addUserToEvent(eventId: String, membersListUpdated: List<String>) {
        val userRef = db.collection(CollectionName.events).document(eventId)
        userRef.update("participants", membersListUpdated)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "addUser: ", it)
            }
    }

    fun addUserLikeToEvent(eventId: String, membersListUpdated: List<String>) {
        val userRef = db.collection(CollectionName.events).document(eventId)
        userRef.update("likers", membersListUpdated)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "addUser: ", it)
            }
    }

    fun updateUserInClub(clubId: String, newList: List<String>) {
        val userRef = db.collection(CollectionName.clubs).document(clubId)
        userRef.update("members", newList)
            .addOnSuccessListener {
                Log.d(TAG, "UpdateUser: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "UpdateUser: ", it)
            }
    }

    fun updateUserAdminStatus(clubId: String, newList: List<String>) {
        val userRef = db.collection(CollectionName.clubs).document(clubId)
        userRef.update("admins", newList)
            .addOnSuccessListener {
                Log.d(TAG, "UpdateAdminStatus: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "UpdateAdminStatus: ", it)
            }
    }

    fun updatePrivacy(clubId: String, newValue: Boolean) {
        val userRef = db.collection(CollectionName.clubs).document(clubId)
        userRef.update("isPrivate", newValue)
            .addOnSuccessListener {
                Log.d(TAG, "UpdatePrivacy: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "UpdatePrivacy: ", it)
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

    fun updateLikeEvent(updatedLikers: List<String>, eventId: String) {
        val ref = db.collection(CollectionName.events).document(eventId)
        ref.update(mapOf(Pair("likers", updatedLikers)))
            .addOnSuccessListener {
                Log.d(TAG, "updateLikeEvent: $ref")
            }
            .addOnFailureListener {
                Log.e(TAG, "updateLikeEvent: ", it)
            }
    }

    // News

    fun getAllEventsOfClub(clubId: String): Query {
        return db.collection(CollectionName.events).whereEqualTo("clubId", clubId)
    }

    fun getNextEvent(clubId: String): Query {
        return getAllEventsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING).limit(1L)
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

    fun deleteNews(newsId: String) {
        val ref = db.collection(CollectionName.news).document(newsId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteNews: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "deleteNews: ", e)
            }
    }

    fun getAllNews() = db.collection(CollectionName.news)

    fun getNews( newsId: String):DocumentReference{
        val ref = db.collection(CollectionName.news)
        return ref.document(newsId)
    }
    fun getAllNewsOfClub(clubId: String): Query {
        return getAllNews().whereEqualTo("clubId", clubId)
    }

    // User

    fun getCurrentUser(): DocumentReference {
        return db.collection(CollectionName.users).document(uid!!)
    }

    fun getAllUsers() = db.collection(CollectionName.users)

    // Requests

    fun getRequestsFromClub(clubId: String) =
        db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests)

    fun addRequest(clubId: String, request: Request) {
        val ref = db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests).document()
        val requestWithId = request.apply { id = ref.id }
        ref.set(requestWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addRequest: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addRequestFail: ", e)
            }
    }

    fun acceptRequest(clubId: String, requestId: String, memberListWithNewUser: List<String>, changeMapForRequest: Map<String, Any>) {
        val ref = db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests).document(requestId)
        ref.update(changeMapForRequest)
            .addOnSuccessListener {
                Log.d(TAG, "acceptRequest: $ref")
                updateUserInClub(clubId, memberListWithNewUser)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "acceptRequestFail: ", e)
            }
    }

    fun declineRequest(clubId: String, requestId: String) {
        val ref = db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests).document(requestId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Request deleted: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "RequestDeletionFail: ", e)
            }
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

    fun getFile(path: String): StorageReference {
        return storage.reference.child(path)
    }

    fun getAllFiles(path: String): Task<ListResult> {
        return storage.reference.child(path).listAll()
    }
}

class CollectionName {
    companion object {
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
        const val users = "users"
        const val requests = "requests"
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
    @get:PropertyName("fName")
    @set:PropertyName("fName")
    var fName: String = "",
    @get:PropertyName("lName")
    @set:PropertyName("lName")
    var lName: String = "",
    val phone: String = "",
    val email: String = "",
    val interests: List<String> = listOf(),
    val firstTime: Boolean = true,
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
    @get:PropertyName("isPrivate")
    @set:PropertyName("isPrivate")
    var isPrivate: Boolean = false,
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
    val participants: List<String> = listOf(),
    val likers: List<String> = listOf()
) : Serializable

data class News(
    var id: String = "",
    val clubId: String = "",
    val headline: String = "",
    val newsContent: String = "",
    val date: Timestamp = Timestamp.now(),
): Serializable


data class Request(
    var id: String = "",
    val userId: String = "",
    val acceptedStatus: Boolean = false,
    val timeAccepted: Timestamp? = null,
    val message: String = "",
    val requestSent: Timestamp = Timestamp.now()
)