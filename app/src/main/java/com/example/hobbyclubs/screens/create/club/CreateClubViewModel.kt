package com.example.hobbyclubs.screens.create.club

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.screens.create.event.notifyObserver

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
    val imagesAsBitmap = MutableLiveData<MutableList<Bitmap>>()
    val logoAsBitmap = MutableLiveData<Bitmap>()
    private val selectedImagesAsBitmaps = mutableListOf<Bitmap>()

    fun convertUriToBitmap(bannerImages: List<Uri>? = null, logo: Uri? = null, context: Context) {
        bannerImages?.let {
            it.forEach { image ->
            val source = ImageDecoder.createSource(context.contentResolver, image)
            val bitmap = ImageDecoder.decodeBitmap(source)
            selectedImagesAsBitmaps.add(bitmap)
            Log.d("imageList", selectedImagesAsBitmaps.toString())
        }
            imagesAsBitmap.value = selectedImagesAsBitmaps
            imagesAsBitmap.notifyObserver()
        }
        logo?.let {
            val source = ImageDecoder.createSource(context.contentResolver, it)
            val bitmap = ImageDecoder.decodeBitmap(source)
            logoAsBitmap.value = bitmap
        }
    }
    var count = 0
    fun storeBitmapsOnFirebase(listToStore: List<Bitmap>, logo: Bitmap, clubId: String) {
        listToStore.forEach { bitmap ->
            firebase.sendClubImage(imageName = if (count == 0) "banner" else "$count.jpg", clubId = clubId, imageBitmap = bitmap)
            count += 1
        }
        firebase.sendClubImage("logo", clubId = clubId, imageBitmap = logo)
    }

    fun temporarilyStoreImages(bannerUri: MutableList<Uri>? = null, logoUri: Uri? = null) {
        bannerUri?.let { selectedBannerImages.value = it }
        logoUri?.let { selectedClubLogo.value = it }
    }

    fun emptySelection(banner: Boolean? = null, logo: Boolean? = null) {
        banner?.let { selectedBannerImages.value = mutableListOf() }
        logo?.let { selectedClubLogo.value = Uri.EMPTY }
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

    fun addClub(club: Club) : String {
        return firebase.addClub(club,)
    }
}