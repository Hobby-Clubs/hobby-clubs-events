package com.example.hobbyclubs.screens.create.club

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import com.google.firebase.firestore.FieldValue

/**
 * Create club view model for handling club creation functions
 */
class CreateClubViewModel : ViewModel() {

    val firebase = FirebaseHelper
    val currentUser = MutableLiveData<User>()

    val currentCreationProgressPage = MutableLiveData<Int>()

    // club details
    val clubName = MutableLiveData<TextFieldValue?>()
    val clubDescription = MutableLiveData<TextFieldValue>()
    val selectedClubLogo = MutableLiveData<Uri?>()
    val selectedBannerImage = MutableLiveData<Uri?>()

    // contact information
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()

    // socials
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val givenLinksLiveData = MutableLiveData<Map<String, String>>(mapOf())

    // club privacy
    val publicSelected = MutableLiveData<Boolean>()
    val privateSelected = MutableLiveData<Boolean>()
    val clubIsPrivate = MutableLiveData<Boolean>()


    /**
     * Change to wanted page
     * @param page page number to change to
     */
    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }

    /**
     * Update club name value based on newVal
     * @param newVal value to put into clubName
     */
    fun updateClubName(newVal: TextFieldValue) {
        clubName.value = newVal
    }

    /**
     * Update club description based on newVal
     * @param newVal value to put into clubDescription
     */
    fun updateClubDescription(newVal: TextFieldValue) {
        clubDescription.value = newVal
    }

    /**
     * Update club privacy selection
     * @param leftVal Boolean for public selected
     * @param rightVal Boolean for private selected
     */
    fun updateClubPrivacySelection(leftVal: Boolean, rightVal: Boolean) {
        publicSelected.value = leftVal
        privateSelected.value = rightVal
        clubIsPrivate.value = rightVal
    }

    /**
     * Update contact info name based on newVal
     * @param newVal value to put into contactInfoName
     */
    fun updateContactInfoName(newVal: TextFieldValue) {
        contactInfoName.value = newVal
    }

    /**
     * Update contact info email based on newVal
     * @param newVal value to put into contactInfoEmail
     */
    fun updateContactInfoEmail(newVal: TextFieldValue) {
        contactInfoEmail.value = newVal
    }

    /**
     * Update contact info number based on newVal
     * @param newVal value to put into contactInfoNumber
     */
    fun updateContactInfoNumber(newVal: TextFieldValue) {
        contactInfoNumber.value = newVal
    }

    /**
     * Update current link name based on newVal
     * @param newVal value to put into currentLinkName
     */
    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    /**
     * Update current link url based on newVal
     * @param newVal value to put into currentLinkURL
     */
    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    /**
     * Add link to list to post on creation
     * @param pair pair of link name and url address
     */
    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap

        }
    }

    /**
     * Clear link fields after adding pair to given links list
     */
    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    /**
     * Store images on firebase and update clubs
     * logoUri and bannerUri fields with a URI.
     *
     * @param bannerUri uri of image selected for banner when creating club
     * @param logoUri uri of image selected for logo when creating club
     * @param clubId UID for the selected club
     */
    fun storeImagesOnFirebase(bannerUri: Uri, logoUri: Uri, clubId: String) {

        // For Banner
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

        // For Logo
        firebase.addPic(logoUri, "${CollectionName.clubs}/$clubId/logo")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("logoUri", FieldValue.arrayUnion(downloadUrl))
                    )
                    firebase.updateEventDetails(clubId, changeMap)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
    }

    /**
     * Temporarily store images in a MutableLiveData
     * for showing them on the screen before actual creation.
     *
     * @param bannerUri uri of image selected for banner when creating club
     * @param logoUri uri of image selected for logo when creating club
     */
    fun temporarilyStoreImages(bannerUri: Uri? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImage.value = it }
        logoUri?.let { selectedClubLogo.value = it }
    }

    /**
     * Get current users data from firebase
     */
    fun getCurrentUser() {
        firebase.getCurrentUser().get()
            .addOnSuccessListener { data ->
                val fetchedUser = data.toObject(User::class.java)
                fetchedUser?.let { currentUser.postValue(it) }
            }
            .addOnFailureListener {
                Log.e("FetchUser", "getUserFail: ", it)
            }
    }

    /**
     * Quick fill options for contact information text fields
     * @param user User object to take information from
     */
    fun quickFillOptions(user: User) {
        contactInfoName.value = TextFieldValue("${user.fName} ${user.lName}")
        contactInfoEmail.value = TextFieldValue(user.email)
        contactInfoNumber.value = TextFieldValue(user.phone)
    }

    /**
     * Add club to firebase
     * @param club Club object to add
     * @return returns clubId
     */
    fun addClub(club: Club): String {
        return firebase.addClub(club)
    }
}