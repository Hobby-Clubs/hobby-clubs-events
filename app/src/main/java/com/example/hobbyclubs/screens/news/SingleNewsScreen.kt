package com.example.hobbyclubs.screens.news

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    news?.let { news ->
        Scaffold() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                horizontalAlignment = Alignment.Start,
            ) {
                NewsContent(vm, news)


//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(rememberScrollState()),
//                horizontalAlignment = Alignment.Start,
//            ) {
//                TopAppBar(
//                    title = {},
//                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
//                    navigationIcon = {
//
//                    }
//                )
//            }

            }
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun NewsContent(
    vm: SingleScreenViewModel = viewModel(),
    news: News
) {
    val getImage by vm.newsUri.observeAsState()
    var club: Club? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (club == null) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            AsyncImage(
                model = getImage,
                contentDescription = "news image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(text = news.headline, fontSize = 20.sp)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = it.name, fontSize = 16.sp)
                }
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                val dateFormatted = sdf.format(news.date.toDate())
                Text(text = dateFormatted, fontWeight = FontWeight.Light, fontSize = 12.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = news.newsContent,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.size(32.dp))
                    Text(
                        text = "Published by someone",
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}