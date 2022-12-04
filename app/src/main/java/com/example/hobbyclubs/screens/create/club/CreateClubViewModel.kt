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

class CreateClubViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val currentUser = MutableLiveData<User>()
    val currentCreationProgressPage = MutableLiveData<Int>()
    val clubName = MutableLiveData<TextFieldValue?>()
    val clubDescription = MutableLiveData<TextFieldValue>()
    val contactInfoName = MutableLiveData<TextFieldValue>()
    val contactInfoEmail = MutableLiveData<TextFieldValue>()
    val contactInfoNumber = MutableLiveData<TextFieldValue>()
    val currentLinkName = MutableLiveData<TextFieldValue>()
    val currentLinkURL = MutableLiveData<TextFieldValue>()
    val selectedBannerImage = MutableLiveData<Uri?>()
    val selectedClubLogo = MutableLiveData<Uri?>()
    val leftSelected = MutableLiveData<Boolean>()
    val rightSelected = MutableLiveData<Boolean>()
    val clubIsPrivate = MutableLiveData<Boolean>()

    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }

    fun updateEventName(newVal: TextFieldValue) {
        clubName.value = newVal
    }

    fun updateEventDescription(newVal: TextFieldValue) {
        clubDescription.value = newVal
    }

    fun updateClubPrivacySelection(leftVal: Boolean, rightVal: Boolean) {
        leftSelected.value = leftVal
        rightSelected.value = rightVal
        clubIsPrivate.value = rightVal
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

    fun updateCurrentLinkName(newVal: TextFieldValue) {
        currentLinkName.value = newVal
    }

    fun updateCurrentLinkURL(newVal: TextFieldValue) {
        currentLinkURL.value = newVal
    }

    val givenLinksLiveData = MutableLiveData<Map<String, String>>(mapOf())

    fun addLinkToList(pair: Pair<String, String>) {
        givenLinksLiveData.value?.let {
            val newMap = it.toMutableMap().apply { put(pair.first, pair.second) }
            println(newMap)
            givenLinksLiveData.value = newMap

        }
    }

    fun clearLinkFields() {
        currentLinkName.value = null
        currentLinkURL.value = null
    }

    fun storeImagesOnFirebase(bannerUri: Uri, logoUri: Uri, clubId: String) {
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
                        Pair("logoUri", FieldValue.arrayUnion(downloadUrl))
                    )
                    firebase.updateEventDetails(clubId, changeMap)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
    }

    fun temporarilyStoreImages(bannerUri: Uri? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImage.value = it }
        logoUri?.let { selectedClubLogo.value = it }
    }

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

    fun quickFillOptions(user: User) {
        contactInfoName.value = TextFieldValue("${user.fName} ${user.lName}")
        contactInfoEmail.value = TextFieldValue(user.email)
        contactInfoNumber.value = TextFieldValue(user.phone)
    }

    fun addClub(club: Club): String {
        return firebase.addClub(club)
    }
}