package com.example.hobbyclubs.screens.clubmembers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.Request
import com.example.hobbyclubs.api.User

class ClubMembersViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val listOfMembers = MutableLiveData<List<User>>(listOf())
    val listOfRequests = MutableLiveData<List<Request>>(listOf())

    private fun getClubMembers(clubMembers: List<String>) {
        listOfMembers.value = listOf()
        clubMembers.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    Log.d("getMembers", it.toString())
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfMembers.value = listOfMembers.value?.plus(listOf(user))
                    }
                    Log.d("getMembers", listOfMembers.value.toString())
                }
                .addOnFailureListener { e ->
                    Log.e("getMembers", "gettingMembers failed: ", e)
                }
        }
    }

    fun getClub(clubId: String) {
        firebase.getClub(uid = clubId).addSnapshotListener { data, e ->
            data ?: run {
                Log.e("FetchClub", "getClubFail: ", e)
                return@addSnapshotListener
            }
            val fetchedClub = data.toObject(Club::class.java)
            fetchedClub?.let {
                println("hello")
                selectedClub.postValue(it)
                getClubMembers(it.members)
            }
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

    fun kickUserFromClub(clubId: String, userId: String) {
        firebase.updateUserInClub(clubId = clubId, userId = userId, remove = true)
    }

    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(Request::class.java)
                Log.d("fetchNews", fetchedRequests.toString())
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

    fun acceptJoinRequest(
        clubId: String,
        requestId: String,
        userId: String,
        changeMapForRequest: Map<String, Any>
    ) {
        firebase.acceptRequest(
            clubId = clubId,
            requestId = requestId,
            userId = userId,
            changeMapForRequest = changeMapForRequest
        )
    }
    fun declineJoinRequest(clubId: String, requestId: String) {
        firebase.declineRequest(clubId, requestId)
    }
}