package com.example.hobbyclubs.screens.home

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.compose.clubTileBg
import com.example.compose.clubTileBorder
import com.example.compose.md_theme_light_error
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.DrawerScreen
import com.example.hobbyclubs.general.LazyColumnHeader
import com.example.hobbyclubs.general.MenuTopBar
import com.example.hobbyclubs.navigation.NavRoutes
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, vm: HomeScreenViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val myClubs by vm.myClubs.observeAsState(listOf())

    DrawerScreen(
        navController = navController,
        topBar = { MenuTopBar(drawerState = drawerState, hasSearch = true) },
        drawerState = drawerState,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            stickyHeader {
                LazyColumnHeader(text = "My Clubs")
            }
            items(myClubs) {
                MyClubTile(club = it, vm = vm) {

                }
            }
        }
    }
}

@Composable
fun MyClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    vm: HomeScreenViewModel,
    onClick: () -> Unit
) {
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var nextEvent: Event? by remember { mutableStateOf(null) }
    var newsAmount: Int? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (picUri == null) {
            vm.getBanner(club.ref)
                .downloadUrl
                .addOnSuccessListener {
                    picUri = it
                }
        }
        if (nextEvent == null) {
            vm.getNextEvent(club.ref)
                .get()
                .addOnSuccessListener {
                    val next = it.toObjects(Event::class.java)
                    if (next.size == 0) {
                        return@addOnSuccessListener
                    }
                    nextEvent = next[0]
                }
        }
        if (newsAmount == null) {
            vm.getNews(club.ref)
                .get()
                .addOnSuccessListener {
                    val news = it.toObjects(News::class.java)
//                    if (news.size == 0) {
//                        return@addOnSuccessListener
//                    }
                    newsAmount = news.size
                }
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(2.125f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(clubTileBg),
        border = BorderStroke(1.dp, clubTileBorder),
    ) {
        Row(Modifier.fillMaxSize()) {
            AsyncImage(
                modifier = Modifier.aspectRatio(0.75f),
                model = picUri,
                contentDescription = "banner",
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = club.name, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(clubTileBorder)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UpcomingEvents(
                        modifier = Modifier.fillMaxHeight(),
                        upcoming = nextEvent,
                        onClick = {})
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(clubTileBorder)
                    )
                    NewsIconSection(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        amount = newsAmount
                    ) {

                    }
                }

            }
        }
    }
}

@Composable
fun NewsIconSection(modifier: Modifier = Modifier, amount: Int?, onClick: () -> Unit) {
    Box(modifier = modifier.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(47.dp)) {
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = Icons.Filled.Newspaper,
                contentDescription = "news"
            )
            if (amount != null) {
                Card(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(md_theme_light_error)
                ) {
                    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = amount.toString(),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                }
            }
        }

    }

}

@Composable
fun UpcomingEvents(modifier: Modifier = Modifier, upcoming: Event?, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .aspectRatio(1.45f)
            .clickable { onClick() },
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (upcoming == null) {
            Text(text = "No upcoming events", fontSize = 12.sp, fontWeight = FontWeight.Light)
        } else {
            val sdf = SimpleDateFormat("dd.MM.yyyy", java.util.Locale.ENGLISH)
            val date = sdf.format(upcoming.date.toDate())
            Text(text = "Upcoming events", fontSize = 12.sp, fontWeight = FontWeight.Light)
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = upcoming.name, fontSize = 14.sp)
                Text(text = date, fontSize = 12.sp, fontWeight = FontWeight.Light)
            }
        }
    }
}

@Composable
fun FakeButtonForNavigationTest(destination: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.padding(10.dp)
    ) {
        Text(text = destination)
    }
}

@Composable
fun FakeNavigation(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        FakeButtonForNavigationTest(destination = "Clubs") {
            navController.navigate(NavRoutes.ClubsScreen.route)
        }
        FakeButtonForNavigationTest(destination = "News") {
            navController.navigate(NavRoutes.NewsScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Calendar") {
            navController.navigate(NavRoutes.CalendarScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Club Page") {
            navController.navigate(NavRoutes.ClubPageScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Clubs") {
            navController.navigate(NavRoutes.ClubsScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Log out") {
            FirebaseHelper.logout()
            navController.navigate(NavRoutes.LoginScreen.route)
        }
    }
}