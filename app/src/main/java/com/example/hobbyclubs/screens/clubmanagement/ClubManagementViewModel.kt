package com.example.hobbyclubs.screens.clubmanagement

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

class ClubManagementViewModel() : ViewModel() {

    val firebase = FirebaseHelper
    val currentUser = MutableLiveData<User>()
    val selectedClub = MutableLiveData<Club>()
    val selectedNewsId = MutableLiveData<String>()
    val selectedEventId = MutableLiveData<String>()
    val isPrivate = Transformations.map(selectedClub) {
        it.isPrivate
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val listOfRequests = MutableLiveData<List<ClubRequest>>()

    val clubName = MutableLiveData<TextFieldValue>()
    val clubDescription = MutableLiveData<TextFieldValue>()
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val selectedBannerImage = MutableLiveData<Uri>()
    val selectedClubLogo = MutableLiveData<Uri>()
    val givenLinksLiveData = MutableLiveData<Map<String, String>>(mapOf())

    fun fillPreviousClubData(club: Club) {
        clubName.value = TextFieldValue(club.name)
        clubDescription.value = TextFieldValue(club.description)
        contactInfoEmail.value = TextFieldValue(club.contactEmail)
        contactInfoNumber.value = TextFieldValue(club.contactPhone)
        contactInfoName.value = TextFieldValue(club.contactPerson)
        givenLinksLiveData.value = club.socials
    }

    fun updateClubDetails(clubId: String, changeMap: Map<String, Any>) {
        firebase.updateClubDetails(clubId, changeMap)
    }

    fun deleteClub(clubId: String) {
        firebase.deleteClub(clubId)
    }

    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap

        }
    }

    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    fun updateContactInfoName(newVal: TextFieldValue) {
        contactInfoName.value = newVal
    }

    fun updateContactInfoEmail(newVal: TextFieldValue) {
        contactInfoEmail.value = newVal
    }

    fun updateContactInfoNumber(newVal: TextFieldValue) {
        contactInfoNumber.value = newVal
    }

    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    fun temporarilyStoreImages(bannerUri: Uri? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImage.value = it }
        logoUri?.let { selectedClubLogo.value = it }
    }

    fun replaceClubImage(bannerUri: Uri, logoUri: Uri, clubId: String) {
        firebase.addPic(bannerUri, "${CollectionName.clubs}/$clubId/banner")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("bannerUri", downloadUrl)
                    )
                    firebase.updateClubDetails(clubId, changeMap)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
        firebase.addPic(logoUri, "${CollectionName.clubs}/$clubId/logo")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("logoUri", downloadUrl)
                    )
                    firebase.updateClubDetails(clubId, changeMap)
                    updateClubsNewsOfUriChanges(clubId, downloadUrl)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
    }

    private fun updateClubsNewsOfUriChanges(clubId: String, uri: Uri) {
        firebase.getAllNewsOfClub(clubId).get()
            .addOnSuccessListener {
                val fetched = it.toObjects(News::class.java)
                fetched.forEach { singleNews ->
                    val changeMap = mapOf(
                        Pair("clubImageUri", uri)
                    )
                    firebase.updateNewsDetails(singleNews.id, changeMap)
                }
            }
    }

    fun updateClubName(newVal: TextFieldValue) {
        clubName.value = newVal
    }

    fun updateClubDescription(newVal: TextFieldValue) {
        clubDescription.value = newVal
    }

    fun updateSelection(newsId: String? = null, eventId: String? = null) {
        newsId?.let { selectedNewsId.value = it }
        eventId?.let { selectedEventId.value = it }
    }

    fun updatePrivacy(clubIsPrivate: Boolean, clubId: String) {
        getClub(clubId)
        selectedClub.let {
            firebase.updatePrivacy(clubId = it.value!!.ref, newValue = clubIsPrivate)
        }
    }

    fun getClub(clubId: String) {
        firebase.getClub(uid = clubId)
            .addSnapshotListener() { data, error ->
                data ?: run {
                    Log.e("getClub", "getClub: ", error)
                    return@addSnapshotListener
                }
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
            }
    }

    fun getClubEvents(clubId: String) {
        firebase.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllEvents", "EventFetchFail: ", error)
                    return@addSnapshotListener
                }
//                Log.d("fetchEvents", "getClubEvents: ${data.documents[0].get("date")}")
                val fetchedEvents = data.toObjects(Event::class.java)
//                Log.d("fetchEvents", fetchedEvents.toString())
                listOfEvents.value = fetchedEvents.filter { it.clubId == clubId }
            }
    }

    fun getAllNews(clubId: String) {
        firebase.getAllNews()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllNews", "NewsFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                Log.d("fetchNews", fetchedNews.toString())
                listOfNews.value = fetchedNews.filter { it.clubId == clubId }.sortedBy { it.date }
            }
    }

    fun deleteNews() {
        selectedNewsId.value?.let { id ->
            firebase.deleteNews(id)
        }
    }

    fun deleteEvent() {
        selectedEventId.value?.let { id ->
            firebase.deleteEvent(id)
        }
    }

    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(ClubRequest::class.java)
                Log.d("fetchNews", fetchedRequests.toString())
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}