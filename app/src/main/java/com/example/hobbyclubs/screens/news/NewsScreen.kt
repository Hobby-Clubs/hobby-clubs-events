package com.example.hobbyclubs.screens.news

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.navigation.NavRoutes
import java.text.SimpleDateFormat
import java.util.*

/**
 * News screen
 * This screen contains cards of news, (all news) that each card contain an image, headline, 1 line content,timestamp, and to which club this news belongs.
 * @param navController for Compose navigation
 * @param vm [NewsViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
    vm: NewsViewModel = viewModel(),
) {
    val allNews = vm.listOfNews.observeAsState(listOf())
    LaunchedEffect(Unit) {
        vm.getAllNews()
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            CenterAlignedTopAppBar(title = { Text(text = "News") },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                })
            if (allNews.value.isNotEmpty()) {
                Dashboard(newsList = allNews.value, navController)
            }
        }
    }
}

/**
 * Dashboard
 * This composable, contains the function of the news list
 * @param newsList
 * @param navController for Compose navigation
 */
@Composable
fun Dashboard(newsList: List<News>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        items(newsList) { singleNews ->
            ImageCard(
                news = singleNews,
                vm = NewsViewModel(),
            ) {
                navController.navigate(NavRoutes.SingleNewsScreen.route + "/${singleNews.id}")
            }
        }
    }
}

/**
 * Image card
 * This composable, contains functions fetches the information from the database using functions from the News View Model.
 * @param news
 * @param vm [NewsViewModel]
 * @param onClick
 * @receiver
 */
@Composable
fun ImageCard(
    news: News, vm: NewsViewModel, onClick: () -> Unit
) {
    var club: Club? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (club == null) {
            vm.getClub(news.clubId).get().addOnSuccessListener { data ->
                    val fetchedClub = data.toObject(Club::class.java)
                    fetchedClub?.let { club = it }
                }.addOnFailureListener {
                    Log.e("FetchClub", "getClubFail: ", it)
                }
        }
    }
    club?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(colorScheme.surface),
            border = BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                AsyncImage(
                    model = news.newsImageUri,
                    contentDescription = "news image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .aspectRatio(16f / 9f),
                    error = painterResource(id = R.drawable.nokia_logo)

                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(
                        text = news.headline,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                    Text(text = it.name, fontWeight = FontWeight.Light)
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                    val dateFormatted = sdf.format(news.date.toDate())
                    Text(text = dateFormatted.toString())
                    Text(
                        text = news.newsContent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 15.dp)
                    )
                }
            }
        }
    }
}