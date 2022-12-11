package com.example.hobbyclubs.screens.create.news

import android.net.Uri
import android.util.Log
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

/**
 * Create news view model for create news
 *
 * @constructor Create empty Create news view model
 */
class CreateNewsViewModel : ViewModel() {

    val firebase = FirebaseHelper
    val currentCreationProgressPage = MutableLiveData<Int>()
    val selectedImage = MutableLiveData<Uri?>()
    val headline = MutableLiveData<TextFieldValue?>()
    val newsContent = MutableLiveData<TextFieldValue>()
    val selectedClub = MutableLiveData<String>()
    val clubsJoined = MutableLiveData<List<Club>>()

    init {
        getJoinedClubs()
    }

    /**
     * Store selected image temporarily when picking an image
     *
     * @param uri
     */
    fun storeSelectedImage(uri: Uri?) {
        selectedImage.value = uri
    }

    /**
     * Change page to next or previous page when clicking next/ previous
     *
     * @param page
     */
    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }

    /**
     * Update headline to update the headline textField value.
     *
     * @param newVal
     */
    fun updateHeadline(newVal: TextFieldValue) {
        headline.value = newVal
    }

    /**
     * Update news content to update the content textField value.
     *
     * @param newVal
     */
    fun updateNewsContent(newVal: TextFieldValue) {
        newsContent.value = newVal
    }

    /**
     * Store news image
     * To store news image in FireBase storage.
     * @param picUri
     * @param newsId
     */
    fun storeNewsImage(picUri: Uri, newsId: String) {
        firebase.addPic(picUri, "${CollectionName.news}/$newsId/newsImage.jpg")
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                    val changeMap = mapOf(
                        Pair("newsImageUri", downloadUrl)
                    )
                    firebase.updateNewsDetails(newsId, changeMap)
                }
            }.addOnFailureListener {
                Log.e(FirebaseHelper.TAG, "addPic: ", it)
            }
    }

    /**
     * Update single news with club image uri
     * To add club image uri to the news, so that it can show club image on single news tile.
     * @param clubId
     * @param newsId
     */
    fun updateSingleNewsWithClubImageUri(clubId: String, newsId: String) {
        firebase.getClub(clubId).get().addOnSuccessListener {
                val fetchedClub = it.toObject(Club::class.java)
                fetchedClub?.let { club ->
                    val changeMap = mapOf(
                        Pair("clubImageUri", club.logoUri)
                    )
                    firebase.updateNewsDetails(newsId, changeMap)
                }
            }
    }

    /**
     * Update selected club to assign the news to the selected club from the drop down menu.
     *
     * @param newVal
     */
    fun updateSelectedClub(newVal: String) {
        selectedClub.value = newVal
    }

    /**
     * Get joined clubs
     * To get the clubs that you are a member
     */
    private fun getJoinedClubs() {
        firebase.uid?.let {
            firebase.getAllClubs().whereArrayContains("members", it).get()
                .addOnSuccessListener { data ->
                    val fetchedClubs = data.toObjects(Club::class.java)
                    clubsJoined.value = fetchedClubs
                }
        }
    }

    /**
     * Add news to FireBase
     *
     * @param news News object to add to FireBase
     * @return newsId
     */
    fun addNews(news: News): String {
        return firebase.addNews(news)
    }
}