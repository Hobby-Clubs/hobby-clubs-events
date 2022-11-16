package com.example.hobbyclubs.screens.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class SingleScreenViewModel: ViewModel()  {
    val firebase = FirebaseHelper
    val news = MutableLiveData<News>()


    fun getNews(newsId:String) {
        firebase.getNews(newsId).addSnapshotListener { data, e ->
            data?.let {
                val newsFetched = it.toObject(News::class.java)
                if (newsFetched != null) {
                    news.postValue(newsFetched)
                }
            }
        }
    }
    fun getClub(clubId: String) = firebase.getClub(uid = clubId)
    fun getImage(newsRef: String) =
        FirebaseHelper.getFile("${CollectionName.news}/$newsRef/newsImage.jpg")
}