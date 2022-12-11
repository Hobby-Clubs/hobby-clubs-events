package com.example.hobbyclubs.screens.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User

/**
 * Login and register view model
 *
 * @constructor Create empty Login and register view model
 */
class LoginAndRegisterViewModel : ViewModel() {
    val picUri = MutableLiveData<Uri>()
    val fName = MutableLiveData<String>()
    val lName = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val email = MutableLiveData("")
    val pwd = MutableLiveData("")
    val showRegister = MutableLiveData(false)
    val isLoggedIn = MutableLiveData(false)
    val authException = MutableLiveData<Exception>()

    /**
     * Updates email value
     *
     * @param newVal
     */
    fun updateEmail(newVal: String) {
        email.value = newVal
    }

    /**
     * Updates password value
     *
     * @param newVal
     */
    fun updatePwd(newVal: String) {
        pwd.value = newVal
    }

    /**
     * Updates showRegister value (toggle between login and register forms)
     *
     * @param newVal
     */
    fun updateShowRegister(newVal: Boolean) {
        showRegister.value = newVal
    }

    /**
     * Updates authException value (to show error when authentication failed)
     *
     * @param newVal
     */
    fun updateAuthException(newVal: Exception?) {
        authException.value = newVal
    }

    /**
     * Updates uri for the selected profile pic
     *
     * @param newVal
     */
    fun updateUri(newVal: Uri) {
        picUri.value = newVal
    }

    /**
     * Updates full name
     *
     * @param newVal
     */
    fun updateFName(newVal: String) {
        fName.value = newVal
    }

    /**
     * Updates last name
     *
     * @param newVal
     */
    fun updateLName(newVal: String) {
        lName.value = newVal
    }

    /**
     * Updates phone number
     *
     * @param newVal
     */
    fun updatePhone(newVal: String) {
        phone.value = newVal
    }

    /**
     * Handles the registering of a new user in Firebase auth. If it fails, shows an error to the user
     *
     * @param user
     * @param pwd
     */
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

    /**
     * Handles the login of a user in Firebase auth. If it fails, shows an error to the user
     *
     * @param email
     * @param pwd
     */
    fun login(email: String, pwd: String) {
        FirebaseHelper.login(email, pwd)
            .addOnSuccessListener {
                isLoggedIn.value = true
            }
            .addOnFailureListener {
                updateAuthException(it)
            }
    }

    /**
     * Adds a user document to the users collection in firestore with the firebase auth
     * unique identifier of that user as the new document's reference id
     *
     * @param user
     */
    private fun addUser(user: User) = FirebaseHelper.addUser(user)

    /**
     * Adds the profile pic of the new user to firebase storage
     *
     * @param uri
     * @param uid
     */
    private fun addProfilePic(uri: Uri, uid: String) =
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