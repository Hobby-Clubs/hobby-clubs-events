package com.example.hobbyclubs.screens.createnews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News

class CreateNewsViewModel : ViewModel() {

    val firebase = FirebaseHelper

    val currentCreationProgressPage = MutableLiveData<Int>()
    val selectedImage = MutableLiveData<Uri?>()
    val selectedImageBitmap = MutableLiveData<Bitmap>()
    val headline = MutableLiveData<TextFieldValue?>()
    val newsContent = MutableLiveData<TextFieldValue>()

    fun convertUriToBitmap(image: Uri, context: Context){
        val source = ImageDecoder.createSource(context.contentResolver, image)
        val bitmap = ImageDecoder.decodeBitmap(source)
        selectedImageBitmap.value= bitmap
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
    fun storeNewsImage(bitmap: Bitmap, newsId: String){
        firebase.sendNewsImage(imageId = "newsImage.jpg", newsId, imageBitmap = bitmap)
    }

    fun addNews(news: News)  : String{
      return  firebase.addNews(news)
    }
}

