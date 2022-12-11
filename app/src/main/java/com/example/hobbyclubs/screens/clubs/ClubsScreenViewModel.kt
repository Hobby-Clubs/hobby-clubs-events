package com.example.hobbyclubs.screens.clubs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import kotlin.math.min

/**
 * Clubs screen view model handles the functions for displaying suggested and all clubs
 */
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

    /**
     * Refresh fetches the clubs again from firebase and creates suggestion list from it
     */
    fun refresh() {
        isRefreshing.value = true
        getClubs(suggestedAmount = 3)
    }

    /**
     * Get clubs from firebase and then create suggestion list from it
     * @param suggestedAmount amount of suggestions shown on clubs page
     */
    private fun getClubs(suggestedAmount: Int) {
        FirebaseHelper.getCurrentUser()
            .get()
            .addOnSuccessListener {
                val fetchedUser = it.toObject(User::class.java)
                fetchedUser?.let { user ->
                    currentUser.value = user
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
                                fetchedClubs.sortedByDescending { club -> club.members.size }
                            clubs.value = clubsByMembers

                            val suggestedPool = if (clubsByMembers.size >= 2 * suggestedAmount) {
                                clubsByMembers.filter { club ->
                                    suggestedClubs.value?.contains(club) == false
                                }
                            } else {
                                clubsByMembers
                            }

                            suggestedClubs.value =
                                if (user.interests.isEmpty()) {
                                    suggestedPool
                                        .filter { club -> !club.members.contains(user.uid) }
                                        .shuffled()
                                        .take(suggestedCount)
                                } else {
                                    suggestedPool
                                        .filter { club -> user.interests.contains(club.category) }
                                        .filter { club -> !club.members.contains(user.uid) }
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
}