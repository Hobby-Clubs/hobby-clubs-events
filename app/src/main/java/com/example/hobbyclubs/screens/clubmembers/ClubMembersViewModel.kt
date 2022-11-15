package com.example.hobbyclubs.screens.clubmembers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User

class ClubMembersViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val listOfMembers = MutableLiveData<MutableList<User>>(mutableListOf())

    fun getClubMembers() {
        selectedClub.value?.members?.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    Log.d("getMembers", it.toString())
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { listOfMembers.value!!.add(fetchedUser) }
                    Log.d("getMembers", listOfMembers.value.toString())
                }
                .addOnFailureListener { e ->
                    Log.e("getMembers", "gettingMembers failed: ", e)
                }
        }
    }

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

    fun promoteToAdmin(clubId: String, userId: String) {
        val updatedList = selectedClub.value?.admins?.toMutableList()
        updatedList?.add(userId)
        firebase.updateUserAdminStatus(clubId = clubId, updatedList!!)

    }

//    fun removeAdminStatus(clubId: String, userId: String) {
//        val updatedList = selectedClub.value?.admins?.toMutableList()
//        updatedList?.remove(userId)
//        firebase.updateUserAdminStatus(clubId = clubId, updatedList!!)
//    }
//
//    fun checkIfUserIsAdmin() {
//        isAdmin.value = selectedClub.value?.admins?.contains(currentUser.value!!.uid)
//    }

    fun kickUserFromClub(clubId: String, userId: String) {
        val updatedList = selectedClub.value?.members?.toMutableList()
        updatedList?.remove(userId)
        firebase.updateUserInClub(clubId = clubId, updatedList!!)
    }
}