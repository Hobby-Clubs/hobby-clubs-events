package com.example.hobbyclubs.screens.news

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class NewsViewModel: ViewModel() {
    val firebase = FirebaseHelper
    val listOfNews = MutableLiveData<List<News>>()
    val news = MutableLiveData<News>()
    val newsUri = MutableLiveData<Uri>()
    val selectedNewsClub = MutableLiveData<Club>()



    fun getALlNews(){
      firebase.getAllNews().addSnapshotListener{ list,e ->
          list ?: run {
              Log.e("news","getAllNews: ", e)
              return@addSnapshotListener
          }
          Log.d("getAllNews", list.toString())

          val newsList = list.toObjects(News::class.java)
          Log.d("getAllNews", newsList.toString())
          listOfNews.postValue(newsList)
      }
  }
    fun getClub(clubId: String) = firebase.getClub(uid = clubId)


    fun getImage(newsRef: String) =
        FirebaseHelper.getFile("${CollectionName.news}/$newsRef/newsImage.jpg")


    fun getNews(newsId:String){
        firebase.getNews(newsId).addSnapshotListener{ data,e ->
            data?.let {
                val newsFetched = it.toObject(News::class.java)
                if (newsFetched != null){
                    news.postValue(newsFetched)
                }
            }
        }
    }

    }
