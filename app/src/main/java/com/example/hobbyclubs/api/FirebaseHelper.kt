package com.example.hobbyclubs.api

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.parcelize.Parcelize

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

    fun updateUser(uid: String, changeMap: Map<String, Any?>) {
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

    fun updateClubDetails(clubId: String, changeMap: Map<String, Any>) {
        val ref = db.collection(CollectionName.clubs).document(clubId)
        ref.update(changeMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateClubDetails: $it")
            }
            .addOnFailureListener {
                Log.e(TAG, "updateClubDetails: ", it)
            }
    }

    fun deleteClub(clubId: String) {
        val ref = db.collection(CollectionName.clubs).document(clubId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "deleteClub: $it")
                getAllFiles("${CollectionName.clubs}/$clubId")
                    .addOnSuccessListener { list ->
                        list.items.forEach { ref ->
                            ref.delete()
                            Log.d(TAG, "deleted: ${ref.name}")
                        }
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "deleteClub: ", it)
            }
    }

    fun getClub(uid: String): DocumentReference {
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

    // Events

    fun addEvent(event: Event): String {
        val ref = db.collection(CollectionName.events).document()
        val eventWithId = event.apply { id = ref.id }
        ref.set(eventWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addEvent: $ref")
                event.clubId.let { id ->
                    getNextEvent(id)
                        .get()
                        .addOnSuccessListener {
                            val next = it.toObjects(Event::class.java)[0]
                            updateClubNextEvent(id, next.date)
                        }
                }
                addNewEventNotif(eventId = ref.id, clubId = event.clubId)
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

    fun updateUserInEvent(eventId: String, membersListUpdated: List<String>) {
        val userRef = db.collection(CollectionName.events).document(eventId)
        userRef.update("participants", membersListUpdated)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "addUser: ", it)
            }
    }

    fun updateEventDetails(eventId: String, changeMap: Map<String, Any>) {
        val ref = db.collection(CollectionName.events).document(eventId)
        ref.update(changeMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateEventDetails: $it")
            }
            .addOnFailureListener {
                Log.e(TAG, "updateEventDetails: ", it)
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

    fun updateUserInClub(clubId: String, userId: String, remove: Boolean = false) {
        val userRef = db.collection(CollectionName.clubs).document(clubId)
        val action = if (remove) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        userRef.update("members", action)
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

    fun addNews(news: News): String {
        val ref = db.collection(CollectionName.news).document()
        val newsId = news.apply {
            id = ref.id
        }
        ref.set(newsId)
            .addOnSuccessListener {
                Log.d(TAG, "addNews: $ref")
                val isGeneral = news.clubId.isEmpty()
                if (isGeneral) {
                    addGeneralNewsNotif(newsId = ref.id)
                } else {
                    addClubNewsNotif(newsId = ref.id, clubId = news.clubId)
                }
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

    fun getNews(newsId: String): DocumentReference {
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
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests)
                .document()
        val requestWithId = request.apply { id = ref.id }
        ref.set(requestWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addRequest: $ref")
                addRequestPendingNotif(userId = request.userId, clubId = clubId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addRequestFail: ", e)
            }
    }

    fun acceptRequest(
        clubId: String,
        requestId: String,
        userId: String,
        changeMapForRequest: Map<String, Any>
    ) {
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests)
                .document(requestId)
        ref.update(changeMapForRequest)
            .addOnSuccessListener {
                Log.d(TAG, "acceptRequest: $ref")
                updateUserInClub(clubId, userId)
                addRequestAcceptedNotif(userId = userId, clubId = clubId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "acceptRequestFail: ", e)
            }
    }

    fun declineRequest(clubId: String, requestId: String) {
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.requests)
                .document(requestId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Request deleted: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "RequestDeletionFail: ", e)
            }
    }

    // Notifications

    fun getNotifications(): CollectionReference {
        return db.collection(CollectionName.notifications)
    }

    fun addNewEventNotif(eventId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.EVENT_CREATED.name,
                eventId = eventId,
                clubId = clubId,
            )
        )
    }

    fun addGeneralNewsNotif(newsId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.NEWS_GENERAL.name,
                newsId = newsId
            )
        )
    }

    fun addClubNewsNotif(newsId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.NEWS_CLUB.name,
                newsId = newsId,
                clubId = clubId,
            )
        )
    }

    fun addRequestPendingNotif(userId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.REQUEST_PENDING.name,
                userId = userId,
                clubId = clubId,
            )
        )
    }

    fun addRequestAcceptedNotif(userId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.REQUEST_ACCEPTED.name,
                userId = userId,
                clubId = clubId,
            )
        )
    }

    fun addNotification(notification: NotificationInfo) {
        val ref = db.collection(CollectionName.notifications).document()
        val data = notification.apply { id = ref.id }
        ref.set(data)
            .addOnSuccessListener {
                Log.d(TAG, "addNotification: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addNotification: ", e)
            }
    }

    fun markNotificationAsSeen(notificationId: String) {
        uid?.let {
            db.collection("notifications").document(notificationId)
                .update("readBy", FieldValue.arrayUnion(it))
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

    fun addPic(uri: Uri, path: String) = storage.reference.child(path).putFile(uri)

    fun updateNewsDetails(newsId: String, changeMap: Map<String, Any>) {
        val ref = db.collection(CollectionName.news).document(newsId)
        ref.update(changeMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateClubDetails: $it")
            }
            .addOnFailureListener {
                Log.e(TAG, "updateClubDetails: ", it)
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
        const val notifications = "notifications"
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

@Parcelize
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
    val profilePicUri: String? = null,
    val interests: List<String> = listOf(),
    val firstTime: Boolean = true,
) : Parcelable

@Parcelize
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
    val nextEvent: Timestamp? = null,
    val logoUri: String = "",
    val bannerUri: String = "",
) : Parcelable

@Parcelize
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
    var isPrivate: Boolean = false,
    val admins: List<String> = listOf(),
    val participants: List<String> = listOf(),
    val likers: List<String> = listOf(),
    val bannerUris: List<String> = listOf(),
) : Parcelable

@Parcelize
data class News(
    var id: String = "",
    val clubId: String = "",
    val publisherId: String = "",
    val headline: String = "",
    val newsContent: String = "",
    val date: Timestamp = Timestamp.now(),
    val newsImageUri: String = "",
    val clubImageUri: String = "",
    val usersRead: List<String> = listOf()
) : Parcelable

@Parcelize
data class Request(
    var id: String = "",
    val userId: String = "",
    val profilePicUri: String? = null,
    val acceptedStatus: Boolean = false,
    val timeAccepted: Timestamp? = null,
    val message: String = "",
    val requestSent: Timestamp = Timestamp.now()
) : Parcelable

@Parcelize
data class NotificationInfo(
    var id: String = "",
    val type: String = "",
    val time: Timestamp = Timestamp.now(),
    val userId: String = "",
    val clubId: String = "",
    val eventId: String = "",
    val newsId: String = "",
    val readBy: List<String> = listOf()
) : Parcelable

enum class NotificationType(val channelName: String) {
    EVENT_CREATED("New events"),
    NEWS_CLUB("Club news"),
    NEWS_GENERAL("General news"),
    REQUEST_PENDING("Pending membership requests"),
    REQUEST_ACCEPTED("Accepted membership requests")
}