package com.example.hobbyclubs.screens.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper

class NewsViewModel: ViewModel() {
    val firebase = FirebaseHelper

    val currentNewsPage = MutableLiveData<Int>()
    fun changePageTo(page: Int) {
        currentNewsPage.value = page
    }
}