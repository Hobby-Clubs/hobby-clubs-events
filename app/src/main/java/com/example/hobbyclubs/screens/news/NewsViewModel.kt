package com.example.hobbyclubs.screens.news

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class NewsViewModel: ViewModel() {
    val firebase = FirebaseHelper
    val listOfNews = MutableLiveData<List<News>>()
    val news = MutableLiveData<News>()

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
