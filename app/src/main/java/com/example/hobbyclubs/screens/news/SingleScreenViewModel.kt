package com.example.hobbyclubs.screens.news

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class SingleScreenViewModel: ViewModel()  {
    val firebase = FirebaseHelper
    val selectedNews = MutableLiveData<News>()
    val newsUri = MutableLiveData<Uri>()
    val getNews = MutableLiveData<Club>()


    fun getClub(clubId: String) = firebase.getClub(uid = clubId)

    fun getNews(newsId:String) {
        firebase.getNews(newsId).addSnapshotListener { data, e ->
            data?.let {
                val newsFetched = it.toObject(News::class.java)
                if (newsFetched != null) {
                    selectedNews.postValue(newsFetched)
                }
            }
        }
    }
    fun getImage(newsRef: String) =
        FirebaseHelper.getFile("${CollectionName.news}/$newsRef/newsImage.jpg").downloadUrl
            .addOnSuccessListener { newsUri.value = it }
}