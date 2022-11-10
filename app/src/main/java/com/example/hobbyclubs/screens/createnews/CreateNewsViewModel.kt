package com.example.hobbyclubs.screens.createnews

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class CreateNewsViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentCreationProgressPage = MutableLiveData<Int>()
    val headline = MutableLiveData<TextFieldValue?>()
    val newsContent = MutableLiveData<TextFieldValue>()

    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }
    fun updateHeadline(newVal: TextFieldValue) {
        headline.value = newVal
    }
    fun updateNewsContent(newVal: TextFieldValue) {
        newsContent.value = newVal
    }

    fun addNews(news: News) {
        firebase.addNews(news)
    }
}

