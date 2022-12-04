package com.example.hobbyclubs.screens.create.news

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class CreateNewsViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentCreationProgressPage = MutableLiveData<Int>()
    val selectedImage = MutableLiveData<Uri?>()
    val selectedImageBitmap = MutableLiveData<Bitmap>()
    val headline = MutableLiveData<TextFieldValue?>()
    val newsContent = MutableLiveData<TextFieldValue>()
    val selectedClub = MutableLiveData<String>()
    val clubsJoined = MutableLiveData<List<Club>>()

    init {
        getJoinedClubs()
    }

    fun storeSelectedImage(uri: Uri?){
        selectedImage.value = uri
    }

    fun changePageTo(page: Int) {
        currentCreationProgressPage.value = page
    }
    fun updateHeadline(newVal: TextFieldValue) {
        headline.value = newVal
    }
    fun updateNewsContent(newVal: TextFieldValue) {
        newsContent.value = newVal
    }
    fun storeNewsImage(picUri: Uri, newsId: String){
        firebase.addPic(picUri, "${CollectionName.news}/$newsId/newsImage.jpg")
    }
    fun updateSelectedClub(newVal: String) {
        selectedClub.value = newVal
    }
    fun getJoinedClubs(){
        firebase.uid?.let {
            firebase.getAllClubs().whereArrayContains("members", it)
                .get()
                .addOnSuccessListener { data ->
                    val fetchedClubs = data.toObjects(Club::class.java)
                    clubsJoined.value = fetchedClubs
                }
        }
    }

    fun addNews(news: News)  : String{
      return  firebase.addNews(news)
    }
}
