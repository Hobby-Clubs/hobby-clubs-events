package com.example.hobbyclubs.screens.clubs

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import kotlin.math.min

class ClubsScreenViewModel : ViewModel() {
    companion object {
        const val TAG = "ClubsScreenViewModel"
    }

    val clubs = MutableLiveData<List<Club>>()
    val suggestedClubs = MutableLiveData<List<Club>>()
    val otherClubs = MutableLiveData<List<Club>>()
    val currentUser = MutableLiveData<User>()

    init {
        getClubs(suggestedAmount = 3)
    }

    private fun getClubs(suggestedAmount: Int) {
        FirebaseHelper.getCurrentUser()
            .get()
            .addOnSuccessListener {
                val fetchedUser = it.toObject(User::class.java)
                fetchedUser?.let { u ->
                    currentUser.value = u
                    FirebaseHelper.getAllClubs()
                        .get()
                        .addOnSuccessListener listener@ { clubList ->
                            val fetchedClubs = clubList.toObjects(Club::class.java)
                            println(fetchedClubs)
                            if (fetchedClubs.isEmpty()) {
                                return@listener
                            }
                            val suggested = min(fetchedClubs.size, suggestedAmount)
                            val clubsByMembers =
                                fetchedClubs.sortedByDescending { club -> club.members.size }
                            clubs.value = clubsByMembers
                            suggestedClubs.value =
                                if (u.interests.isEmpty()) {
                                    clubsByMembers.take(suggested)
                                } else {
                                    clubsByMembers.filter { club -> u.interests.contains(club.category) }
                                        .take(suggested)
                                }
                            val other =
                                clubsByMembers.subList(suggested, clubsByMembers.size)
                            otherClubs.value = other
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "getClubs: ", error)
                        }
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "getCurrentUser: ", error)
            }
    }

    fun getLogo(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/logo")

    fun getBanner(clubRef: String) =
        FirebaseHelper.getFile("${CollectionName.clubs}/$clubRef/banner")

    fun addMockClubs(amount: Int, club: Club, logoUri: Uri, bannerUri: Uri) {
        repeat(amount) {
            FirebaseHelper.addClub(club)
        }
    }
}