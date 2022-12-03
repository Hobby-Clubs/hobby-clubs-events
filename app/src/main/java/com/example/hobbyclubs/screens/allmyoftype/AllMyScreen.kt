package com.example.hobbyclubs.screens.allmyoftype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.EventTile
import com.example.hobbyclubs.general.SmallNewsTile
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubs.ClubTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllMyScreen(
    navController: NavController,
    type: String,
    vm: AllMyViewModel = viewModel()
) {
    val selectedTypeText: String = when (type) {
        "club" -> "Clubs"
        "event" -> "Events"
        "news" -> "News"
        else -> ""
    }

    LaunchedEffect(Unit) {
        if (type == "club") {
            vm.fetchAllClubs()
        }
        if (type == "event") {
            vm.fetchAllEvents()
        }
        if (type == "news") {
            vm.fetchAllClubs()
            vm.fetchAllNews()
        }
    }

    val myClubs by vm.myClubs.observeAsState(null)
    val myEvents by vm.myEvents.observeAsState(null)
    val myNews by vm.myNews.observeAsState(null)

    if (myClubs != null || myEvents != null || myNews != null) {
        Scaffold() { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateBottomPadding(), horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "My $selectedTypeText",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfSelectedType(
                    navController = navController,
                    clubs = if (type == "club") myClubs else null,
                    events = if (type == "event") myEvents else null,
                    news = if (type == "news") myNews else null
                )
            }
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    }
}

@Composable
fun ListOfSelectedType(
    navController: NavController,
    clubs: List<Club>? = null,
    events: List<Event>? = null,
    news: List<News>? = null
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        clubs?.let {
            items(it) { club ->
                ClubTile(club = club) {
                    navController.navigate(NavRoutes.ClubPageScreen.route + "/${club.ref}")
                }
            }
        }
        events?.let {
            items(it) { event ->
                EventTile(event = event, navController = navController) {
                    navController.navigate(NavRoutes.EventScreen.route + "/${event.id}")
                }
            }
        }
        news?.let {
            items(it) { singleNews ->
                SmallNewsTile(news = singleNews) {
                    navController.navigate(NavRoutes.SingleNewsScreen.route + "/${singleNews.id}")
                }
            }
        }
    }
}
