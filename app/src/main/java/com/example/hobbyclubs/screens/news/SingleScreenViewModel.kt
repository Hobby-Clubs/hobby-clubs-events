package com.example.hobbyclubs.screens.news

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.*

class SingleScreenViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedNews = MutableLiveData<News>()
    val newsUri = MutableLiveData<Uri>()
    val headline = MutableLiveData<TextFieldValue>()
    val newsContent = MutableLiveData<TextFieldValue>()
    val currentUser = MutableLiveData<User>()
    val publisher = MutableLiveData<User>()
    val isPublisher = Transformations.map(selectedNews) {
        it.publisherId == firebase.uid
    }
    val selectedImage = MutableLiveData<Uri>()
    val loading = MutableLiveData<Boolean>()

    fun updateLoadingStatus(newVal: Boolean) {
        loading.value = newVal
    }

    fun updateHeadline(newVal: TextFieldValue) {
        headline.value = newVal
    }

    fun updateNewsContent(newVal: TextFieldValue) {
        newsContent.value = newVal
    }

    fun updateNewsImage(picUri: Uri, newsId: String) {
        firebase.updateNewsImage(newsId, picUri)
    }

    fun updateNews(newsId: String, changeMap: Map<String, Any>) {
        firebase.updateNewsDetails(newsId, changeMap)
    }

    fun temporarilyStoreImage(newsUri: Uri?) {
        newsUri?.let { selectedImage.value = it }
    }

    fun fillPreviousClubData(news: News) {
        headline.value = TextFieldValue(news.headline)
        newsContent.value = TextFieldValue(news.newsContent)
    }

    fun getCurrentUser() {
        firebase.getCurrentUser().get()
            .addOnSuccessListener { data ->
                val fetchedUser = data.toObject(User::class.java)
                fetchedUser?.let { currentUser.postValue(it) }
            }
            .addOnFailureListener {
                Log.e("FetchUser", "getUserFail: ", it)
            }
    }

    fun getPublisher(publisherId: String) {
        firebase.getUser(publisherId).get()
            .addOnSuccessListener {
                val fetchedUser = it.toObject(User::class.java)
                publisher.value = fetchedUser
            }
            .addOnFailureListener {
                Log.e("getPublisher", "getPublisher: ", it)
            }
    }

    fun getClub(clubId: String) = firebase.getClub(uid = clubId)

    fun getNews(newsId: String) {
        firebase.getNews(newsId).addSnapshotListener { data, e ->
            data ?: run {
                Log.e("getNews", "NewsFetchFail: ", e)
                return@addSnapshotListener
            }
            val newsFetched = data.toObject(News::class.java)
            selectedNews.postValue(newsFetched)
        }
    }

    fun getImage(newsRef: String) =
        FirebaseHelper.getFile("${CollectionName.news}/$newsRef/newsImage.jpg").downloadUrl
            .addOnSuccessListener { newsUri.value = it }
}