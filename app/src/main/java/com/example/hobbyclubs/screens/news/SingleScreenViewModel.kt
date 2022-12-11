package com.example.hobbyclubs.screens.news

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.api.User

/**
 * Single screen view model for handling Single News Screen
 *
 * @constructor Create empty Single screen view model
 */
class SingleScreenViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val selectedNews = MutableLiveData<News>()
    val headline = MutableLiveData<TextFieldValue>()
    val newsContent = MutableLiveData<TextFieldValue>()
    val publisher = MutableLiveData<User>()
    val isPublisher = Transformations.map(selectedNews) {
        it.publisherId == firebase.uid
    }
    val hasRead = Transformations.map(selectedNews) {
        it.usersRead.contains(firebase.uid)
    }
    val selectedImage = MutableLiveData<Uri>()
    val loading = MutableLiveData<Boolean>()

    /**
     * Update loading status after news Edit
     *
     * @param newVal
     */
    fun updateLoadingStatus(newVal: Boolean) {
        loading.value = newVal
    }

    /**
     * Update headline after news Edit
     *
     * @param newVal
     */
    fun updateHeadline(newVal: TextFieldValue) {
        headline.value = newVal
    }

    /**
     * Update news content after news Edit
     *
     * @param newVal
     */
    fun updateNewsContent(newVal: TextFieldValue) {
        newsContent.value = newVal
    }

    /**
     * Update news image after news Edit
     *
     * @param picUri
     * @param newsId selected items newsId
     */
    fun updateNewsImage(picUri: Uri, newsId: String) {
        FirebaseHelper.addPic(picUri, "${CollectionName.news}/$newsId/newsImage.jpg")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("newsImageUri", downloadUrl)
                    )
                    firebase.updateNewsDetails(newsId, changeMap)
                }
            }
            .addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
    }

    /**
     * Update news after clicking Save button for the FireBase
     *
     * @param newsId selected items newsId
     * @param changeMap changes that need to be updated to firebase
     */
    fun updateNews(newsId: String, changeMap: Map<String, Any>) {
        firebase.updateNewsDetails(newsId, changeMap)
    }

    /**
     * Temporarily store image while editing and choosing a new image for the news in the edit news sheet.
     *
     * @param newsUri
     */
    fun temporarilyStoreImage(newsUri: Uri?) {
        newsUri?.let { selectedImage.value = it }
    }

    /**
     * Remove selected image
     */
    fun removeSelectedImage() {
        selectedImage.value = null
    }

    /**
     * Fill previous club data during editing process
     *
     * @param news
     */
    fun fillPreviousClubData(news: News) {
        headline.value = TextFieldValue(news.headline)
        newsContent.value = TextFieldValue(news.newsContent)
    }

    /**
     * Get publisher of news by publisher id, to check if current user is the publisher to be show editing right.
     *
     * @param publisherId
     */
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

    /**
     * Get club of each news by id
     *
     * @param clubId
     */
    fun getClub(clubId: String) = firebase.getClub(clubId = clubId)

    /**
     * Get news fetches the news by id
     *
     * @param newsId selected items newsId
     */
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
}