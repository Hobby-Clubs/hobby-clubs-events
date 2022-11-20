package com.example.hobbyclubs.general

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

class ImageViewModel: ViewModel() {
    val clubLogoUris = MutableLiveData<List<Pair<String, Uri?>>>()
    val eventBannerUris = MutableLiveData<List<Pair<String, Uri?>>>()
    val clubBannerUris = MutableLiveData<List<Pair<String, Uri?>>>()

    fun getEventUris(listOfEvents: List<Event>) {
        val tempList = mutableListOf<Pair<String, Uri?>>()

        listOfEvents.forEach { event ->
            FirebaseHelper.getAllFiles("${CollectionName.events}/${event.id}")
                .addOnSuccessListener { res ->
                    val items = res.items
                    if (items.isEmpty()) {
                        tempList.add(Pair(event.id, null))
                        if (tempList.size == listOfEvents.size) {
                            eventBannerUris.value = tempList.toList()
                        }
                        return@addOnSuccessListener
                    }
                    val bannerRef = items.find { it.name == "0.jpg" } ?: items.first()
                    bannerRef
                        .downloadUrl
                        .addOnSuccessListener {
                            tempList.add(Pair(event.id, it))
                            if (tempList.size == listOfEvents.size) {
                                eventBannerUris.value = tempList.toList()
                            }
                        }
                        .addOnFailureListener {
                            Log.e("getPicUri", "EventTile: ", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("getAllFiles", "EventTile: ", it)
                }
        }
    }
    fun getClubUris(listOfClubs: List<Club>) {
        val tempListLogo = mutableListOf<Pair<String, Uri?>>()
        val tempListBanner = mutableListOf<Pair<String, Uri?>>()

        listOfClubs.forEach { club ->
            FirebaseHelper.getAllFiles("${CollectionName.clubs}/${club.ref}")
                .addOnSuccessListener { res ->
                    val items = res.items
                    val logoRef = items.find { it.name == "logo" }
                    val bannerRef = items.find { it.name == "banner" }
                    if (logoRef == null) {
                        tempListLogo.add(Pair(club.ref, null))
                        if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
                            clubBannerUris.value = tempListBanner.toList()
                            clubLogoUris.value = tempListLogo.toList()
                        }
                        return@addOnSuccessListener
                    } else {
                        logoRef.downloadUrl.addOnSuccessListener {
                            tempListLogo.add(Pair(club.ref, it))
                            if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
                                clubBannerUris.value = tempListBanner.toList()
                                clubLogoUris.value = tempListLogo.toList()
                            }
                        }
                    }
                    if (bannerRef == null) {
                        tempListBanner.add(Pair(club.ref, null))
                        if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
                            clubBannerUris.value = tempListBanner.toList()
                            clubLogoUris.value = tempListLogo.toList()
                        }
                        return@addOnSuccessListener
                    } else {
                        bannerRef.downloadUrl.addOnSuccessListener {
                            tempListBanner.add(Pair(club.ref, it))
                            if (tempListLogo.size == listOfClubs.size && tempListBanner.size == listOfClubs.size) {
                                clubBannerUris.value = tempListBanner.toList()
                                clubLogoUris.value = tempListLogo.toList()
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("getAllFiles", "ClubTile: ", it)
                }
        }
    }
}