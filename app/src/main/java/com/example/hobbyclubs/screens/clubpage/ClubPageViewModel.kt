package com.example.hobbyclubs.screens.clubpage

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper

class ClubPageViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val logoUri = MutableLiveData<Uri>()
    val bannerUri = MutableLiveData<Uri>()

    fun getClub(clubId: String) {
        firebase.getClub(uid = clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { selectedClub.postValue(fetchedClub) }
        }
            .addOnFailureListener {
                Log.e("FetchClub", "getClubFail: ", it)
            }
    }

    fun getLogo(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/logo").downloadUrl
            .addOnSuccessListener { logoUri.value = it }

    fun getBanner(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/banner").downloadUrl
            .addOnSuccessListener { bannerUri.value = it }
}