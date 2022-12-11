package com.example.hobbyclubs.screens.news

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.google.firebase.firestore.Query

/**
 * News view model for handling News Screen
 *
 * @constructor Create empty News view model
 */
class NewsViewModel : ViewModel() {
    val firebase = FirebaseHelper
    val listOfNews = MutableLiveData<List<News>>()
    val news = MutableLiveData<News>()

    /**
     * Get all news
     * Fetch all the news from database
     */
    fun getAllNews() {
        firebase.getAllNews().orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { list, e ->
                list ?: run {
                    Log.e("news", "getAllNews: ", e)
                    return@addSnapshotListener
                }
                val newsList = list.toObjects(News::class.java)
                listOfNews.value = newsList
            }
    }

    /**
     * Get club of each single news
     *
     * @param clubId
     */
    fun getClub(clubId: String) = firebase.getClub(uid = clubId)

}
