package com.example.hobbyclubs.screens.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper

class HomeScreenViewModel : ViewModel() {
    val myClubs = MutableLiveData<List<Club>>()

    init {
        getMyClubs()
    }

    fun getMyClubs() {
        FirebaseHelper.getAllClubs()
            .get()
            .addOnSuccessListener {
                val allClubs = it.toObjects(Club::class.java)
                val sorted = allClubs
                    .filter { club -> club.members.contains(FirebaseHelper.uid) }
                    .sortedBy { club -> club.nextEvent }
                myClubs.value = sorted
            }
    }

    fun getBanner(clubId: String) = FirebaseHelper.getFile("${CollectionName.clubs}/$clubId/banner")

    fun getNextEvent(clubId: String) = FirebaseHelper.getNextEvent(clubId)

    fun getNews(clubId: String) = FirebaseHelper.getAllNewsOfClub(clubId)
}