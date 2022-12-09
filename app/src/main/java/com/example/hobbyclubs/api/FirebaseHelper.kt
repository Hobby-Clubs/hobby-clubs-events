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
import kotlinx.coroutines.tasks.await
import kotlinx.parcelize.Parcelize


/**
 * Firebase helper: Helper containing all Firebase related functions (auth, firestore and storage)
 *
 * @constructor Create empty Firebase helper
 */
object FirebaseHelper {
    const val TAG = "FirebaseHelper"
    private val db get() = Firebase.firestore

    // Firestore-related

    // User

    /**
     * Creates a new user document in the users collection
     *
     * @param user user to be added
     */
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

    /**
     * Gets a reference to a single user document given a user id
     *
     * @param uid user id
     * @return Firebase DocumentReference for a user
     */
    fun getUser(uid: String): DocumentReference {
        return db.collection(CollectionName.users).document(uid)
    }

    /**
     * Returns a reference to the current user's user document
     *
     * @return firestore document reference of the current user
     */
    fun getCurrentUser(): DocumentReference {
        return getUser(uid!!)
    }

    /**
     * Updates user document given its user id and a map of values to be changed
     *
     * @param uid user firestore document reference id
     * @param changeMap map of values to be changed
     */
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

    /**
     * Creates a club document in the clubs collection
     *
     * @param club object representing a hobby club
     * @return Reference id of the created document
     */
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

    /**
     * Updates a document's values given its reference id
     *
     * @param clubId reference id of the document
     * @param changeMap map of the values to be changed
     */
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

    /**
     * Deletes a club given a reference id. Also deletes all related files in Firebase Storage
     *
     * @param clubId club reference id
     */
    fun deleteClub(clubId: String) {
        val ref = db.collection(CollectionName.clubs).document(clubId)
        ref.delete()
            .addOnSuccessListener {
                getAllFiles("${CollectionName.clubs}/$clubId")
                    .addOnSuccessListener { list ->
                        list.items.forEach { ref ->
                            ref.delete()
                        }
                    }
            }
            .addOnFailureListener {
                Log.e(TAG, "deleteClub: ", it)
            }
    }

    /**
     * Retrieves the reference to a single club document given its id
     *
     * @param clubId reference id to the document
     * @return DocumentReference to the club document in Firestore
     */
    fun getClub(clubId: String): DocumentReference {
        return db.collection(CollectionName.clubs).document(clubId)
    }

    /**
     * Returns the reference to the clubs collection in Firestore
     *
     */
    fun getAllClubs() = db.collection(CollectionName.clubs)

    /**
     * Adds or removes a user to/from a club's member array
     *
     * @param clubId firestore reference id of the club to be updated
     * @param userId firebase auth id of the user to be added/removed
     * @param remove if true, remove, if false, adds
     */
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

    /**
     * Adds or removes a user to/from a club's admin array
     *
     * @param clubId firestore reference id of the club to be updated
     * @param userId firebase auth id of the user to be added/removed
     * @param remove if true, remove, if false, adds
     */
    fun updateUserAdminStatus(clubId: String, userId: String, remove: Boolean = false) {
        val action = if (remove) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        val userRef = db.collection(CollectionName.clubs).document(clubId)
        userRef.update("admins", action)
            .addOnSuccessListener {
                Log.d(TAG, "UpdateAdminStatus: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "UpdateAdminStatus: ", it)
            }
    }

    /**
     * Updates the value of isPrivate on a club document
     *
     * @param clubId reference id of the club to modify
     * @param newValue new value for isPrivate
     */
    fun updatePrivacy(clubId: String, newValue: Boolean) {
        val changeMap = mapOf(Pair("isPrivate", newValue))
        updateClubDetails(clubId, changeMap)
    }

    // Events

    /**
     * Adds an event document in the events collection. Also updates the nextEvent value of the club
     * document it is attached to and adds a corresponding document to the notifications collection.
     *
     * @param event event object to be added
     * @return Reference id of the added document
     */
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
                            updateClubDetails(id, mapOf(Pair("nextEvent", next.date)))
                        }
                }
                addNewEventNotif(eventId = ref.id, clubId = event.clubId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addEvent: ", e)
            }
        return ref.id
    }

    /**
     * Deletes an event given its document referenceId
     *
     * @param eventId event document firestore reference id
     */
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

    /**
     * Gets the reference to a single event document given its reference id
     *
     * @param eventId document firestore reference id
     * @return firestore DocumentReference
     */
    fun getEvent(eventId: String): DocumentReference {
        return db.collection(CollectionName.events).document(eventId)
    }

    /**
     * Adds or removes a participant to/from an event. The participant's user id is added/removed to/from
     * a participants array
     *
     * @param eventId reference id to the event document to be added
     * @param userId user id of the participant to be added/removed
     * @param remove if true, removes participant, if false, adds participant
     */
    fun updateJoinEvent(eventId: String, userId: String, remove: Boolean = false) {
        val action = if (remove) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        val userRef = db.collection(CollectionName.events).document(eventId)
        userRef.update("participants", action)
            .addOnSuccessListener {
                Log.d(TAG, "addUser: " + "success (${userRef.id})")
            }
            .addOnFailureListener {
                Log.e(TAG, "addUser: ", it)
            }
    }

    /**
     * Adds or removes the current user's id to/from the likers array of an event document
     *
     * @param eventId reference id of the event to be modified
     * @param remove if true, removes, if false, adds
     */
    fun updateLikeEvent(eventId: String, remove: Boolean = false) {
        uid?.let { uid ->
            val action = if (remove) FieldValue.arrayRemove(uid) else FieldValue.arrayUnion(uid)
            val ref = db.collection(CollectionName.events).document(eventId)
            ref.update(mapOf(Pair("likers", action)))
                .addOnSuccessListener {
                    Log.d(TAG, "updateLikeEvent: $ref")
                }
                .addOnFailureListener {
                    Log.e(TAG, "updateLikeEvent: ", it)
                }
        }
    }

    /**
     * Updates an event document given its reference id and a map of values to be changed
     *
     * @param eventId firestore document reference
     * @param changeMap map of values to be updated
     */
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

    /**
     * Returns the reference to the events collection in Firestore
     *
     */
    fun getAllEvents() = db.collection(CollectionName.events)

    /**
     * Returns a query that refers to all the events related to a given club
     *
     * @param clubId firestore reference id of the club
     * @return a query to all the event documents of a club
     */
    fun getAllEventsOfClub(clubId: String): Query {
        return db.collection(CollectionName.events).whereEqualTo("clubId", clubId)
    }

    /**
     * Returns a query to the next upcoming event of a club
     *
     * @param clubId firestore reference id of the club
     * @return
     */
    private fun getNextEvent(clubId: String): Query {
        val now = Timestamp.now()
        return getAllEventsOfClub(clubId)
            .whereGreaterThanOrEqualTo("date", now)
            .orderBy("date", Query.Direction.ASCENDING)
            .limit(1L)
    }

    // News

    /**
     * Adds a news document to the news collection. Also creates a notification for the news
     *
     * @param news news to add
     * @return firestore reference id to the news added
     */
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

    /**
     * Deletes a news document given its reference id
     *
     * @param newsId firestore reference id to the news
     */
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

    /**
     * Returns the reference to the news collection
     *
     */
    fun getAllNews() = db.collection(CollectionName.news)

    /**
     * Returns the reference to a single news document given its reference id
     *
     * @param newsId
     * @return
     */
    fun getNews(newsId: String): DocumentReference {
        val ref = db.collection(CollectionName.news)
        return ref.document(newsId)
    }

    /**
     * Updates a news document in firestore
     *
     * @param newsId
     * @param changeMap
     */
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

    /**
     * Returns a query that refers to all the news related to a club
     *
     * @param clubId firestore reference id of the club
     * @return a query to all the news of the club
     */
    fun getAllNewsOfClub(clubId: String): Query {
        return getAllNews().whereEqualTo("clubId", clubId)
    }

    // Club Requests

    /**
     * Returns a reference to the requests collection of a given club
     *
     * @param clubId firestore reference id of the club
     */
    fun getRequestsFromClub(clubId: String) =
        db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.clubRequests)

    /**
     * Adds a membership request to a club's requests collection
     *
     * @param clubId firestore reference id of the club to be changed
     * @param request request to be added
     */
    fun addClubRequest(clubId: String, request: ClubRequest) {
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.clubRequests)
                .document()
        val requestWithId = request.apply { id = ref.id }
        ref.set(requestWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addRequest: $ref")
                addClubRequestNotif(userId = request.userId, clubId = clubId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addRequestFail: ", e)
            }
    }

    /**
     * Updates the status of a request to accepted and the time of acceptance. Also adds the user id
     * of new member to the members array of the given club and creates a notification to notify the
     * admins of the club.
     *
     * @param clubId firestore reference to the club to which a member is added
     * @param requestId firestore reference to the request to be updated
     * @param userId firebase auth id of the user to be added
     * @param changeMapForRequest map of changes to be made (status and time)
     */
    fun acceptClubRequest(
        clubId: String,
        requestId: String,
        userId: String,
        changeMapForRequest: Map<String, Any>
    ) {
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.clubRequests)
                .document(requestId)
        ref.update(changeMapForRequest)
            .addOnSuccessListener {
                Log.d(TAG, "acceptRequest: $ref")
                updateUserInClub(clubId, userId)
                addClubRequestAcceptedNotif(userId = userId, clubId = clubId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "acceptRequestFail: ", e)
            }
    }

    /**
     * Deletes a request from a clubs requests collection, effectively declining ones request to
     * join a given club
     *
     * @param clubId firestore reference id of the club to be changed
     * @param requestId firestore reference id of the request to be deleted
     */
    fun declineClubRequest(clubId: String, requestId: String) {
        val ref =
            db.collection(CollectionName.clubs).document(clubId).collection(CollectionName.clubRequests)
                .document(requestId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Request deleted: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "RequestDeletionFail: ", e)
            }
    }

    // Event Requests

    /**
     * Returns a reference to all the participation requests of an event
     *
     * @param eventId firestore reference id of the event
     */
    fun getRequestsFromEvent(eventId: String) =
        db.collection(CollectionName.events).document(eventId).collection(CollectionName.eventRequests)

    /**
     * Adds a participation request to the requests collection of an event. Also creates a notification
     * to notify the admins of that event
     *
     * @param eventId firestore reference to the event
     * @param request request to be added
     */
    fun addEventRequest(eventId: String, request: EventRequest) {
        val ref =
            db.collection(CollectionName.events).document(eventId).collection(CollectionName.eventRequests)
                .document()
        val requestWithId = request.apply { id = ref.id }
        ref.set(requestWithId)
            .addOnSuccessListener {
                Log.d(TAG, "addEventRequest: $ref")
                addEventRequestNotif(userId = request.userId, eventId = eventId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addEventRequestFail: ", e)
            }
    }

    /**
     * Updates the status of a request to accepted and the time of acceptance. Also adds the user id
     * of new participant to the participants array of the given event and creates a notification to
     * notify the admins of the event.
     *
     * @param eventId firestore reference to the event to which a participant is added
     * @param requestId firestore reference to the request to be updated
     * @param userId firebase auth id of the user to be added
     * @param changeMapForRequest map of changes to be made (status and time)
     */
    fun acceptEventRequest(
        eventId: String,
        requestId: String,
        userId: String,
        changeMapForRequest: Map<String, Any>
    ) {
        val ref =
            db.collection(CollectionName.events).document(eventId).collection(CollectionName.eventRequests)
                .document(requestId)
        ref.update(changeMapForRequest)
            .addOnSuccessListener {
                Log.d(TAG, "acceptEventRequest: $ref")
                updateJoinEvent(eventId, userId)
                addEventRequestAcceptedNotif(userId = userId, eventId = eventId)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "acceptEventRequestFail: ", e)
            }
    }

    /**
     * Deletes a request from an event's requests collection, effectively declining ones request to
     * join a given event
     *
     * @param eventId firestore reference id of the event to be changed
     * @param requestId firestore reference id of the request to be deleted
     */
    fun declineEventRequest(eventId: String, requestId: String) {
        val ref =
            db.collection(CollectionName.events).document(eventId).collection(CollectionName.eventRequests)
                .document(requestId)
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Event request deleted: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "EventRequestDeletionFail: ", e)
            }
    }

    /**
     * Returns whether the current user has requested to join a given event
     *
     * @param eventId firestore reference id of the event
     * @return true if there is a request, false otherwise
     */
    suspend fun getHasRequested(eventId: String): Boolean {
        uid?.let { uid ->
            val allRequests = getRequestsFromEvent(eventId)
                .get()
                .await()
                .toObjects(EventRequest::class.java)

            return allRequests.filter { !it.acceptedStatus }.find { it.userId == uid } != null
        } ?: return false
    }

    // Notifications

    /**
     * Returns a firestore reference to the notifications collection
     *
     */
    fun getNotifications() = db.collection(CollectionName.notifications)

    /**
     * Adds a notification document to the notifications collection to inform that an event
     * was created by a club
     *
     * @param eventId firestore reference id of the event
     * @param clubId firestore reference id of the club
     */
    fun addNewEventNotif(eventId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.EVENT_CREATED.name,
                eventId = eventId,
                clubId = clubId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform of a general news
     *
     * @param newsId firestore reference id of the news
     */
    private fun addGeneralNewsNotif(newsId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.NEWS_GENERAL.name,
                newsId = newsId
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform of a club-related news
     *
     * @param newsId firestore reference id of the news
     * @param clubId firestore reference id of the club
     */
    private fun addClubNewsNotif(newsId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.NEWS_CLUB.name,
                newsId = newsId,
                clubId = clubId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform the admins of a club
     * which a user is trying to join
     *
     * @param userId firebase auth id of the user making the request
     * @param clubId firestore reference of the club
     */
    private fun addClubRequestNotif(userId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.CLUB_REQUEST_PENDING.name,
                userId = userId,
                clubId = clubId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform a user that their
     * request to join a club was accepted
     *
     * @param userId firebase auth id of the user making the request
     * @param clubId firestore reference of the club
     */
    private fun addClubRequestAcceptedNotif(userId: String, clubId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.CLUB_REQUEST_ACCEPTED.name,
                userId = userId,
                clubId = clubId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform the admins of an event
     * which a user is trying to join
     *
     * @param userId firebase auth id of the user making the request
     * @param eventId firestore reference of the event
     */
    private fun addEventRequestNotif(userId: String, eventId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.EVENT_REQUEST_PENDING.name,
                userId = userId,
                eventId = eventId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection to inform a user that their
     * request to join an event was accepted
     *
     * @param userId firebase auth id of the user making the request
     * @param eventId firestore reference of the club
     */
    private fun addEventRequestAcceptedNotif(userId: String, eventId: String) {
        addNotification(
            NotificationInfo(
                type = NotificationType.EVENT_REQUEST_ACCEPTED.name,
                userId = userId,
                eventId = eventId,
            )
        )
    }

    /**
     * Adds a notification document to the notifications collection
     *
     * @param notification notification to be added
     */
    private fun addNotification(notification: NotificationInfo) {
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

    /**
     * Adds the current user's id to a notifications readBy array to mark is as read
     *
     * @param notificationId firestore reference id to the notification
     */
    fun markNotificationAsSeen(notificationId: String) {
        uid?.let {
            db.collection("notifications").document(notificationId)
                .update("readBy", FieldValue.arrayUnion(it))
        }
    }

    // Auth

    private val auth = Firebase.auth
    // unique identifier of the current signed in user
    val uid get() = auth.uid
    val currentUser = auth.currentUser

    /**
     * Attempts to login to firebase auth with an email and a password
     *
     * @param email
     * @param pwd password
     * @return a task to get the result of the authentication attempt
     */
    fun login(email: String, pwd: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, pwd)
    }

    /**
     * Attempts to register to firebase auth with an email and a password
     *
     * @param email
     * @param pwd password
     * @return a task to get the result of the authentication attempt
     */
    fun register(email: String, pwd: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, pwd)
    }

    /**
     * Logs the user out of firebase auth
     *
     */
    fun logout() {
        auth.signOut()
    }

    // Storage

    private val storage = Firebase.storage

    /**
     * Adds a picture file to firebase storage
     *
     * @param uri uri of the picture
     * @param path path where the file should be stored in firebase storage
     */
    fun addPic(uri: Uri, path: String) = storage.reference.child(path).putFile(uri)

    /**
     * Returns the reference to a file in firebase storage
     *
     * @param path path of the file to retrieve
     * @return firebase storage reference to the file
     */
    fun getFile(path: String): StorageReference {
        return storage.reference.child(path)
    }

    /**
     * Attempts to get all the files contained in a firebase storage directory
     *
     * @param path of the directory
     * @return a task to get all the files in the directory
     */
    private fun getAllFiles(path: String): Task<ListResult> {
        return storage.reference.child(path).listAll()
    }
}

/**
 * Contains all the firestore collection names
 *
 */
class CollectionName {
    companion object {
        const val clubs = "clubs"
        const val events = "events"
        const val news = "news"
        const val users = "users"
        const val clubRequests = "clubRequests"
        const val eventRequests = "eventRequests"
        const val notifications = "notifications"
    }
}


/**
 * Contains all the club categories
 *
 */
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

/**
 * Represents a user of the app
 *
 * @property uid unique identifier in firebase auth
 * @property fName first name
 * @property lName last name
 * @property phone phone number
 * @property email email address
 * @property profilePicUri uri of profile picture
 * @property interests interests in terms of club categories
 * @property firstTime true if it is the first time the user logs in
 */
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

/**
 * Represents a club
 *
 * @property ref firestore document reference id
 * @property name name of club
 * @property description description of club
 * @property admins userIds of admin members
 * @property members userIds of members
 * @property contactPerson full name of contact person
 * @property contactPhone phone number of contact person
 * @property contactEmail email of contact person
 * @property socials social media links
 * @property isPrivate privacy setting of the club
 * @property created date of creation
 * @property category category of the club
 * @property nextEvent next upcoming event date
 * @property logoUri uri of club logo
 * @property bannerUri uri of club page banner
 */
@Parcelize
data class Club(
    var ref: String = "",
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

/**
 * Represents an event
 *
 * @property id firestore document reference id
 * @property clubId reference id of club the event relates to. Can be empty
 * @property name name of event
 * @property description description of event
 * @property date date of event
 * @property address address of event
 * @property participantLimit max number of participants
 * @property linkArray map of links to socials
 * @property contactInfoName full name of contact person
 * @property contactInfoEmail email of contact person
 * @property contactInfoNumber phone number of contact person
 * @property isPrivate privacy setting of event
 * @property admins list of event admins
 * @property participants list of participants
 * @property likers list of users who liked the event
 * @property bannerUris uris of event banner pictures
 */
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
    @get:PropertyName("isPrivate")
    @set:PropertyName("isPrivate")
    var isPrivate: Boolean = false,
    val admins: List<String> = listOf(),
    val participants: List<String> = listOf(),
    val likers: List<String> = listOf(),
    val bannerUris: List<String> = listOf(),
) : Parcelable

/**
 * Represents a news article
 *
 * @property id firestore document reference id
 * @property clubId reference id of the club it relates to
 * @property publisherId user id of the publisher
 * @property headline headline of the article
 * @property newsContent content of the article
 * @property date date of the article
 * @property newsImageUri uri of news image
 * @property clubImageUri uri of club logo the news relates to
 * @property usersRead list of user ids of users who read the article
 */
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

/**
 * Represents a membership request to a private club
 *
 * @property id firestore document reference id
 * @property userId id of the user who wants to join
 * @property acceptedStatus status of the request(pending or accepted)
 * @property timeAccepted time that the request was accepted
 * @property message message included in the request
 * @property requestSent time the request was sent
 */
@Parcelize
data class ClubRequest(
    var id: String = "",
    val userId: String = "",
    val acceptedStatus: Boolean = false,
    val timeAccepted: Timestamp? = null,
    val message: String = "",
    val requestSent: Timestamp = Timestamp.now()
) : Parcelable

/**
 * Represents a participation request to a private event
 *
 * @property id firestore document reference id
 * @property userId id of the user who wants to join
 * @property acceptedStatus status of the request(pending or accepted)
 * @property timeAccepted time that the request was accepted
 * @property message message included in the request
 * @property requestSent time the request was sent
 */
@Parcelize
data class EventRequest(
    var id: String = "",
    val userId: String = "",
    val acceptedStatus: Boolean = false,
    val timeAccepted: Timestamp? = null,
    val message: String = "",
    val requestSent: Timestamp = Timestamp.now()
) : Parcelable

/**
 * Represents the info necessary to sort and display a notification
 *
 * @property id firestore document reference id
 * @property type type of notification [NotificationType]
 * @property time time of the notification
 * @property userId user id of targeted receiver (can be empty)
 * @property clubId club id of the club concerned (can be empty)
 * @property eventId event id of the related event (can be empty)
 * @property newsId news id of the news article shared (can be empty)
 * @property readBy list of user ids of readers
 */
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

/**
 * Types of notifications
 *
 * @property channelName name of the relevant notification channel
 */
enum class NotificationType(val channelName: String) {
    // New event created
    EVENT_CREATED("New events"),
    // News related to a club
    NEWS_CLUB("Club news"),
    // General news
    NEWS_GENERAL("General news"),
    // Pending club membership request
    CLUB_REQUEST_PENDING("Pending membership requests"),
    // Accepted club membership request
    CLUB_REQUEST_ACCEPTED("Accepted membership requests"),
    // Pending event participation request
    EVENT_REQUEST_PENDING("Event participation requests"),
    // Accepted event participation request
    EVENT_REQUEST_ACCEPTED("Accepted membership requests"),
}