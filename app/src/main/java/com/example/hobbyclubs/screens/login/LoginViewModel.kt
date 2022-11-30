package com.example.hobbyclubs.screens.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User

class LoginViewModel : ViewModel() {
    val picUri = MutableLiveData<Uri>()
    val fName = MutableLiveData<String>()
    val lName = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val email = MutableLiveData("")
    val pwd = MutableLiveData("")
    val showRegister = MutableLiveData(false)
    val isLoggedIn = MutableLiveData(false)
    val authException = MutableLiveData<Exception>()

    fun updateEmail(newVal: String) {
        email.value = newVal
    }

    fun updatePwd(newVal: String) {
        pwd.value = newVal
    }

    fun updateShowRegister(newVal: Boolean) {
        showRegister.value = newVal
    }

    fun updateAuthException(newVal: Exception?) {
        authException.value = newVal
    }

    fun updateUri(newVal: Uri) {
        picUri.value = newVal
    }

    fun updateFName(newVal: String) {
        fName.value = newVal
    }

    fun updateLName(newVal: String) {
        lName.value = newVal
    }

    fun updatePhone(newVal: String) {
        phone.value = newVal
    }

    fun register(user: User, pwd: String) {
        FirebaseHelper.register(user.email, pwd)
            .addOnSuccessListener { res ->
                isLoggedIn.value = true
                val newUid = res.user?.uid ?: "0"
                val newUser = user.apply { uid = newUid }
                addUser(newUser)
                picUri.value?.let { uri ->
                    addProfilePic(uri, newUid)
                }
            }
            .addOnFailureListener {
                updateAuthException(it)
            }
    }

    fun login(email: String, pwd: String) {
        FirebaseHelper.login(email, pwd)
            .addOnSuccessListener {
                isLoggedIn.value = true
            }
            .addOnFailureListener {
                updateAuthException(it)
            }
    }

    fun addUser(user: User) = FirebaseHelper.addUser(user)

    fun addProfilePic(uri: Uri, uid: String) =
        FirebaseHelper.addPic(uri, "${CollectionName.users}/$uid")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("profilePicUri", downloadUrl)
                    )
                    FirebaseHelper.updateUser(uid, changeMap)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
}