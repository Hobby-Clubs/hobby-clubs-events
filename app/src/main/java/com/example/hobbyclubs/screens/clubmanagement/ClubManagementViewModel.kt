package com.example.hobbyclubs.screens.clubmanagement

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

/**
 * Club management view model for handling club management functions
 */
class ClubManagementViewModel() : ViewModel() {

    val firebase = FirebaseHelper
    val currentUser = MutableLiveData<User>()

    // Management
    val selectedClub = MutableLiveData<Club>()
    val selectedNewsId = MutableLiveData<String>()
    val selectedEventId = MutableLiveData<String>()
    val isPrivate = Transformations.map(selectedClub) {
        it.isPrivate
    }
    val listOfEvents = MutableLiveData<List<Event>>(listOf())
    val listOfNews = MutableLiveData<List<News>>(listOf())
    val listOfRequests = MutableLiveData<List<ClubRequest>>()

    // Editing club
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

    /**
     * Fill previous club data to show previous data before editing.
     * @param club Club object fetched from firebase
     */
    fun fillPreviousClubData(club: Club) {
        clubName.value = TextFieldValue(club.name)
        clubDescription.value = TextFieldValue(club.description)
        contactInfoEmail.value = TextFieldValue(club.contactEmail)
        contactInfoNumber.value = TextFieldValue(club.contactPhone)
        contactInfoName.value = TextFieldValue(club.contactPerson)
        givenLinksLiveData.value = club.socials
    }

    /**
     * Update club details updates club details based on change map
     * @param clubId UID for the club you have selected on home or club screen
     * @param changeMap changes that need to be updated to firebase
     */
    fun updateClubDetails(clubId: String, changeMap: Map<String, Any>) {
        firebase.updateClubDetails(clubId, changeMap)
    }

    /**
     * Delete club
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun deleteClub(clubId: String) {
        firebase.deleteClub(clubId)
    }

    /**
     * Add link to list of pairs of clubId and url
     * @param pair
     */
    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap
        }
    }

    /**
     * Update current link name
     * @param newVal value to put into currentLinkName
     */
    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    /**
     * Update current link url
     * @param newVal value to put into currentLinkURL
     */
    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    /**
     * Update contact info name
     * @param newVal value to put into contactInfoName
     */
    fun updateContactInfoName(newVal: TextFieldValue) {
        contactInfoName.value = newVal
    }

    /**
     * Update contact info email
     * @param newVal value to put into contactInfoEmail
     */
    fun updateContactInfoEmail(newVal: TextFieldValue) {
        contactInfoEmail.value = newVal
    }

    /**
     * Update contact info number
     * @param newVal value to put into contactInfoNumber
     */
    fun updateContactInfoNumber(newVal: TextFieldValue) {
        contactInfoNumber.value = newVal
    }

    /**
     * Clear link fields after adding a link pair to the list
     */
    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    /**
     * Temporarily store images to display them on the screen before sending to firebase.
     * @param bannerUri uri of image selected from gallery for banner
     * @param logoUri uri of image selected from gallery for logo
     */
    fun temporarilyStoreImages(bannerUri: Uri? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImage.value = it }
        logoUri?.let { selectedClubLogo.value = it }
    }

    /**
     * Remove selected image
     * @param banner if banner should be removed
     * @param logo if logo should be removed
     */
    fun removeSelectedImage(banner: Boolean = false, logo: Boolean = false) {
        if (banner) selectedBannerImage.value = null
        if (logo) selectedClubLogo.value = null
    }

    /**
     * Replace club images on firebase storage. Adds pictures to firebase after creation of club.
     * @param bannerUri uri of image selected from gallery for banner
     * @param logoUri uri of image selected from gallery for logo
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun replaceClubImage(bannerUri: Uri, logoUri: Uri, clubId: String) {
        // Banner
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

        // Logo
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

    /**
     * Update clubs news of uri changes when existing club logo has been updated
     *
     * @param clubId UID for the club you have selected on home or club screen
     * @param uri uri of image selected from gallery for logo
     */
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

    /**
     * Update club name
     * @param newVal value to put into clubName
     */
    fun updateClubName(newVal: TextFieldValue) {
        clubName.value = newVal
    }

    /**
     * Update club description
     * @param newVal value to put into clubDescription
     */
    fun updateClubDescription(newVal: TextFieldValue) {
        clubDescription.value = newVal
    }

    /**
     * Update selection of news or event ids
     * @param newsId selected items newsId
     * @param eventId selected itemsEventId
     */
    fun updateSelection(newsId: String? = null, eventId: String? = null) {
        newsId?.let { selectedNewsId.value = it }
        eventId?.let { selectedEventId.value = it }
    }

    /**
     * Update privacy of club
     * @param clubIsPrivate if club is private this is true otherwise false
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun updatePrivacy(clubIsPrivate: Boolean, clubId: String) {
        getClub(clubId)
        selectedClub.let {
            firebase.updatePrivacy(clubId = it.value!!.ref, newValue = clubIsPrivate)
        }
    }

    /**
     * Get club data from firebase
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun getClub(clubId: String) {
        firebase.getClub(clubId = clubId)
            .addSnapshotListener() { data, error ->
                data ?: run {
                    Log.e("getClub", "getClub: ", error)
                    return@addSnapshotListener
                }
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
            }
    }

    /**
     * Get club events of club from firebase
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun getClubEvents(clubId: String) {
        firebase.getAllEvents()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllEvents", "EventFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedEvents = data.toObjects(Event::class.java)
                listOfEvents.value = fetchedEvents.filter { it.clubId == clubId }
            }
    }

    /**
     * Get all news of club from firebase
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun getAllNews(clubId: String) {
        firebase.getAllNews()
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllNews", "NewsFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedNews = data.toObjects(News::class.java)
                listOfNews.value = fetchedNews.filter { it.clubId == clubId }.sortedBy { it.date }
            }
    }

    /**
     * Delete news from firebase
     */
    fun deleteNews() {
        selectedNewsId.value?.let { id ->
            firebase.deleteNews(id)
        }
    }

    /**
     * Delete event from firebase
     */
    fun deleteEvent() {
        selectedEventId.value?.let { id ->
            firebase.deleteEvent(id)
        }
    }

    /**
     * Get all join requests of club from firebase that are not accepted
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(ClubRequest::class.java)
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }
}