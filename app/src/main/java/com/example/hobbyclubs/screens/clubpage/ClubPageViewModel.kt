package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query

/**
 * Club page view model handles functions related to the club page.
 */
class ClubPageViewModel : ViewModel() {

    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()
    val hasJoinedClub = Transformations.map(selectedClub) { it.members.contains(firebase.uid) }
    private val clubsRequests = MutableLiveData<List<ClubRequest>>()
    val hasRequested = Transformations.map(clubsRequests) { list ->
        list.any { it.userId == firebase.uid && !it.acceptedStatus }
    }
    val isAdmin = Transformations.map(selectedClub) { it.admins.contains(firebase.uid) }
    val clubIsPrivate = Transformations.map(selectedClub) { it.isPrivate }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val joinClubDialogText = MutableLiveData<TextFieldValue>()

    /**
     * Update dialog text
     *
     * @param newVal value when textFieldValue is changed
     */
    fun updateDialogText(newVal: TextFieldValue) {
        joinClubDialogText.value = newVal
    }

    /**
     * Get club data from firebase when opening club page.
     *
     * @param clubId UID for the selected club
     */
    fun getClub(clubId: String) {
        firebase.getClub(clubId = clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getClub", "getClub: ", error)
                    return@addSnapshotListener
                }
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
            }
    }

    /**
     * Get all news data from firebase of selected club.
     *
     * @param clubId UID for the selected club
     * @param fromHomeScreen If user pressed the button on the home screens club tiles that shows
     * unread news this will be true, if user pressed from the club page this will be false
     */
    fun getAllNews(clubId: String, fromHomeScreen: Boolean = false) {
        firebase.getAllNewsOfClub(clubId).orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllNews", "getAllNews: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                if (fromHomeScreen){
                    listOfNews.value = fetchedNews.filter { !it.usersRead.contains(FirebaseHelper.uid) }
                } else {
                    listOfNews.value = fetchedNews
                }
            }
    }


    /**
     * Get all events of selected club from firebase. Ordered by date.
     * @param clubId UID for the selected club
     */
    fun getClubEvents(clubId: String) {
        val now = Timestamp.now()
        firebase.getAllEventsOfClub(clubId).orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("ClubPageViewModel", "getClubEvents: ", error)
                    return@addSnapshotListener
                }
                val fetchedEvents = data.toObjects(Event::class.java)
                listOfEvents.value = fetchedEvents.filter { it.date >= now }
            }
    }

    /**
     * Get selected event data from firebase
     * @param eventId UID for the selected event
     */
    fun getEvent(eventId: String) = firebase.getEvent(eventId)

    /**
     * Join club adds user to the clubs members array on firebase.
     * @param clubId UID for the selected club
     */
    fun joinClub(clubId: String) {
        firebase.uid?.let { uid ->
            firebase.updateUserInClub(clubId = clubId, userId = uid)
            getClub(clubId)
        }
    }

    /**
     * Leave club removes user from clubs members array on firebase
     * @param clubId UID for the selected club
     */
    fun leaveClub(clubId: String) {
        firebase.uid?.let { uid ->
            firebase.updateUserInClub(clubId = clubId, userId = uid, remove = true)
            getClub(clubId)
        }
    }

    /**
     * Send join club request allows user to send a request to the selected club.
     * @param clubId UID for the selected club
     * @param request club join request with all the details given.
     */
    fun sendJoinClubRequest(clubId: String, request: ClubRequest) {
        firebase.addClubRequest(clubId = clubId, request = request)
    }

    /**
     * Get all join requests data from firebase for selected club.
     *
     * @param clubId UID for the selected club
     */
    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(ClubRequest::class.java)
                clubsRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

}