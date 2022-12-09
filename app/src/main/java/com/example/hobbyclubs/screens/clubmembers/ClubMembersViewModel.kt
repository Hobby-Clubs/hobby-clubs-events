package com.example.hobbyclubs.screens.clubmembers

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.ClubRequest
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User

/**
 * Club members view model handles functions when viewing clubs members
 */
class ClubMembersViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedClub = MutableLiveData<Club>()
    val listOfMembers = MutableLiveData<List<User>>(listOf())
    val listOfRequests = MutableLiveData<List<ClubRequest>>(listOf())

    /**
     * Get club members details from firebase
     * @param clubMembers List of userId for fetching data of user from firebase.
     */
    private fun getClubMembers(clubMembers: List<String>) {
        listOfMembers.value = listOf()
        clubMembers.forEach { memberId ->
            firebase.getUser(memberId).get()
                .addOnSuccessListener {
                    val fetchedUser = it.toObject(User::class.java)
                    fetchedUser?.let { user ->
                        listOfMembers.value = listOfMembers.value?.plus(listOf(user))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("getMembers", "gettingMembers failed: ", e)
                }
        }
    }

    /**
     * Get club data from firebase
     * @param clubId
     */
    fun getClub(clubId: String) {
        firebase.getClub(uid = clubId).addSnapshotListener { data, e ->
            data ?: run {
                Log.e("FetchClub", "getClubFail: ", e)
                return@addSnapshotListener
            }
            val fetchedClub = data.toObject(Club::class.java)
            fetchedClub?.let {
                selectedClub.postValue(it)
                getClubMembers(it.members)
            }
        }
    }

    /**
     * Promote to a normal member to admin
     * @param clubId UID for the club you have selected on home or club screen
     * @param userId userId to know which user to promote
     */
    fun promoteToAdmin(clubId: String, userId: String) {
        val updatedList = selectedClub.value?.admins?.toMutableList()
        updatedList?.add(userId)
        firebase.updateUserAdminStatus(clubId = clubId, updatedList!!)
    }

    /**
     * Kick user from club
     * @param clubId UID for the club you have selected on home or club screen
     * @param userId userId to know which user to kick
     */
    fun kickUserFromClub(clubId: String, userId: String) {
        firebase.updateUserInClub(clubId = clubId, userId = userId, remove = true)
    }

    /**
     * Get all join requests for that selected club from firebase
     * @param clubId UID for the club you have selected on home or club screen
     */
    fun getAllJoinRequests(clubId: String) {
        firebase.getRequestsFromClub(clubId)
            .addSnapshotListener { data, error ->
                data ?: run {
                    Log.e("getAllRequests", "RequestFetchFail: ", error)
                    return@addSnapshotListener
                }
                val fetchedRequests = data.toObjects(ClubRequest::class.java)
                listOfRequests.value = fetchedRequests.filter { !it.acceptedStatus }
            }
    }

    /**
     * Accept join request as admin of a club
     *
     * @param clubId UID for the club you have selected on home or club screen
     * @param requestId UID for the request to accept
     * @param userId userId to add to the clubs members list
     * @param changeMapForRequest update the requests acceptedStatus to true and timeAccepted to time when accepted
     */
    fun acceptJoinRequest(
        clubId: String,
        requestId: String,
        userId: String,
        changeMapForRequest: Map<String, Any>
    ) {
        firebase.acceptClubRequest(
            clubId = clubId,
            requestId = requestId,
            userId = userId,
            changeMapForRequest = changeMapForRequest
        )
    }

    /**
     * Decline join request
     *
     * @param clubId UID for the club you have selected on home or club screen
     * @param requestId UID for the request to decline
     */
    fun declineJoinRequest(clubId: String, requestId: String) {
        firebase.declineClubRequest(clubId, requestId)
    }
}