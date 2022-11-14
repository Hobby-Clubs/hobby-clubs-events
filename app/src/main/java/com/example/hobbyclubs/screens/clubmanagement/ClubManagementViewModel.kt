package com.example.hobbyclubs.screens.clubmanagement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClubManagementViewModel() : ViewModel() {
    val isPrivate = MutableLiveData<Boolean>()

    fun updatePrivacy(isPrivate: Boolean) {
        this.isPrivate.value = isPrivate
    }
}