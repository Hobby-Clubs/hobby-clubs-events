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
    val suggestedClubs = MutableLiveData<List<Club>>(listOf())
    val currentUser = MutableLiveData<User>()
    val isRefreshing = MutableLiveData(false)

    init {
        refresh()
    }

    fun refresh() {
        isRefreshing.value = true
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
                        .addOnSuccessListener listener@{ clubList ->
                            val fetchedClubs = clubList.toObjects(Club::class.java)
                            if (fetchedClubs.isEmpty()) {
                                isRefreshing.postValue(false)
                                return@listener
                            }
                            val suggestedCount = min(fetchedClubs.size, suggestedAmount)
                            val clubsByMembers =
                                fetchedClubs.filter { club -> !club.members.contains(u.uid) }
                                    .sortedByDescending { club -> club.members.size }
                            clubs.value = clubsByMembers

                            val suggestedPool = if (clubsByMembers.size >= 2 * suggestedAmount) {
                                clubsByMembers.filter { club ->
                                    suggestedClubs.value?.contains(club) == false
                                }
                            } else {
                                clubsByMembers
                            }

                            suggestedClubs.value =
                                if (u.interests.isEmpty()) {
                                    suggestedPool.shuffled().take(suggestedCount)
                                } else {
                                    suggestedPool.filter { club -> u.interests.contains(club.category) }
                                        .shuffled()
                                        .take(suggestedCount)
                                }

                            isRefreshing.postValue(false)
                        }
                        .addOnFailureListener { error ->
                            Log.e(TAG, "getClubs: ", error)
                            isRefreshing.postValue(false)
                        }
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "getCurrentUser: ", error)
                isRefreshing.postValue(false)
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