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
    val selectedBannerImages = MutableLiveData<MutableList<Uri>>()
    val selectedClubLogo = MutableLiveData<Uri>()
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
    private var count = 0
    fun storeImagesOnFirebase(listToStore: List<Uri>, logo: Uri, clubId: String) {
        listToStore.forEach { uri ->
            val imageName = if (count == 0) "banner" else "$count.jpg"
            firebase.addPic(uri, "${CollectionName.clubs}/$clubId/$imageName")
            count += 1
        }
        firebase.addPic(logo, "${CollectionName.clubs}/$clubId/logo")
    }

    fun temporarilyStoreImages(bannerUri: MutableList<Uri>? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImages.value = it }
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