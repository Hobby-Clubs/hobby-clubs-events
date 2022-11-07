package com.example.hobbyclubs.api

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable

object FirebaseHelper {
    const val TAG = "FirebaseHelper"
    val db get() = Firebase.firestore
    val uid get() = Firebase.auth.uid

    fun addClub(club: Club) {
        val ref = db.collection("clubs")
        ref.add(club)
            .addOnSuccessListener {
                Log.d(TAG, "addClub: $ref")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "addClub: ", e)
            }
    }

    fun getClubs() {

    }
}

data class Club(
    val name: String
): Serializable