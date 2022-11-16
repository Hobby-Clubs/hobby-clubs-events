package com.example.hobbyclubs.screens.news

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.News
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleNewsScreen(
    navController: NavController,
    vm: SingleScreenViewModel = viewModel(),
    newsId: String
    ) {


    val context = LocalContext.current
    val news by vm.selectedNews.observeAsState(null)
    LaunchedEffect(Unit) {
        vm.getNews(newsId)
        vm.getImage(newsId)
    }
    news?.let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {

                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    }
                )
                NewsContent(context, vm, it)
            }
        }

    }
}

@Composable
fun NewsContent(
    context: Context,
    vm: SingleScreenViewModel = viewModel(),
    news: News
){
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val getImage by vm.newsUri.observeAsState()
    var club: Club? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {

        if(club == null){
            vm.getClub(news.clubId).get()
                .addOnSuccessListener { data ->
                    val fetchedClub = data.toObject(Club::class.java)
                    fetchedClub?.let { club = it }
                }
                .addOnFailureListener {
                    Log.e("FetchClub", "getClubFail: ", it)
                }
        }
    }
    club?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    //.padding(10.dp)
                    .fillMaxSize(),
            ) {
                AsyncImage(
                    model = getImage,
                    contentDescription = "news image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                        .clip(RoundedCornerShape(16.dp))

                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = news.headline, fontWeight = FontWeight.Bold)
                        Text(text = it.name, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                        val dateFormatted = sdf.format(news.date.toDate())
                        Text(text = dateFormatted.toString())
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text(
                            text = news.newsContent,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

            }
        }
    }
}