package com.example.hobbyclubs.screens.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper

class LoginViewModel: ViewModel() {
    val email = MutableLiveData("")
    val pwd = MutableLiveData("")
    val showRegister = MutableLiveData(false)
    val isLoggedIn = MutableLiveData(false)
    val authException = MutableLiveData<Exception>()

    init {
        isLoggedIn.value = FirebaseHelper.currentUser != null
    }

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

    fun register(email: String, pwd: String) {
        FirebaseHelper.register(email, pwd)
            .addOnSuccessListener {
                isLoggedIn.value = true
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
}