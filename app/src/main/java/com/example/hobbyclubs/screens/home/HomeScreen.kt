package com.example.hobbyclubs.screens.home

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.notifications.AlarmReceiver
import com.example.hobbyclubs.screens.clubs.ClubTile
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeScreenViewModel = viewModel(),
    imageVm: ImageViewModel = viewModel()
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val allClubs by vm.allClubs.observeAsState(listOf())
    val allEvents by vm.allEvents.observeAsState(listOf())
    var fabIsExpanded by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val searchInput by vm.searchInput.observeAsState("")
    val focusManager = LocalFocusManager.current
    val isFirst by vm.isFirstTimeUser.observeAsState()

    LaunchedEffect(isFirst) {
        isFirst?.let {
            if (it) {
                navController.navigate(NavRoutes.FirstTimeScreen.route)
            }
        }
    }

    LaunchedEffect(allClubs) {
        if (allClubs.isNotEmpty()) {
            imageVm.getClubUris(allClubs)
        }
    }

    LaunchedEffect(allEvents) {
        if (allEvents.isNotEmpty()) {
            imageVm.getEventUris(allEvents)
        }
    }

    DrawerScreen(
        navController = navController,
        topBar = {
            MenuTopBar(
                drawerState = drawerState,
                searchBar = {
                    TopSearchBar(
                        modifier = Modifier,
                        input = searchInput,
                        onTextChange = {
                            showSearch = it.isNotBlank()
                            vm.updateInput(it)
                        },
                        onCancel = {
                            vm.updateInput("")
                            showSearch = false
                            focusManager.clearFocus()
                        }
                    )
                },
                settingsIcon = {
                    IconButton(onClick = {
                        navController.navigate(NavRoutes.SettingsScreen.route)
                    }) {
                        Icon(
                            Icons.Outlined.Settings,
                            null,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                    }
                }
            )
        },
        drawerState = drawerState,
        fab = {
            if (!showSearch) {
                FAB(isExpanded = fabIsExpanded, navController = navController) {
                    fabIsExpanded = !fabIsExpanded
                }
            }
        }
    ) {
        if (!showSearch) {
            MainScreenContent(
                vm = vm,
                imageVm = imageVm,
                navController = navController,
            )
        } else {
            SearchUI(vm = vm, navController = navController, imageVm)
        }
    }
}

@Composable
fun SearchUI(vm: HomeScreenViewModel, navController: NavController, imageVm: ImageViewModel) {
    val allClubs by vm.allClubs.observeAsState(listOf())
    val searchInput by vm.searchInput.observeAsState("")
    val allEvents by vm.allEvents.observeAsState(listOf())
    val myClubsEvents by remember {
        derivedStateOf {
            allEvents.filter {
                allClubs.map { club -> club.ref }.contains(it.clubId)
            }
        }
    }

    val clubsFiltered by remember {
        derivedStateOf {
            if (searchInput.isNotBlank()) {
                allClubs.filter { club -> club.name.contains(searchInput, ignoreCase = true) }
            } else {
                allClubs
            }
        }
    }

    val eventsFiltered by remember {
        derivedStateOf {
            if (searchInput.isNotBlank()) {
                myClubsEvents.filter { event ->
                    event.name.contains(
                        searchInput,
                        ignoreCase = true
                    )
                }
            } else {
                myClubsEvents
            }
        }
    }

    val eventBannerUris by imageVm.eventBannerUris.observeAsState(listOf())
    val clubLogoUris by imageVm.clubLogoUris.observeAsState(listOf())
    val clubBannerUris by imageVm.clubBannerUris.observeAsState(listOf())

    var clubsExpanded by remember { mutableStateOf(true) }
    var eventsExpanded by remember { mutableStateOf(true) }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { clubsExpanded = !clubsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = "Clubs (${clubsFiltered.size})",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                )
                Icon(
                    modifier = Modifier.rotate(if (clubsExpanded) 180f else 0f),
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "expand"
                )
            }
        }
        if (clubsExpanded) {
            if (clubBannerUris.isNotEmpty() && clubLogoUris.isNotEmpty()) {
                items(clubsFiltered) { club ->
                    val logoUri = clubLogoUris.find { it.first == club.ref }?.second
                    val bannerUri = clubBannerUris.find { it.first == club.ref }?.second
                    ClubTile(
                        club = club,
                        logoUri = logoUri,
                        bannerUri = bannerUri
                    ) {
                        navController.navigate(NavRoutes.ClubPageScreen.route + "/${club.ref}")
                    }
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { eventsExpanded = !eventsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = "Events (${eventsFiltered.size})",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                )
                Icon(
                    modifier = Modifier.rotate(if (eventsExpanded) 180f else 0f),
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "expand"
                )
            }
        }
        if (eventsExpanded) {
            if (eventBannerUris.isNotEmpty()) {
                items(eventsFiltered) { event ->
                    val picUri = eventBannerUris.find { it.first == event.id }?.second
                    EventTile(event = event, picUri = picUri) {
                        navController.navigate(NavRoutes.EventScreen.route + "/${event.id}")
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenContent(
    vm: HomeScreenViewModel,
    imageVm: ImageViewModel,
    navController: NavController,
) {
    val myClubs by vm.myClubs.observeAsState(listOf())
    val myEvents by vm.myEvents.observeAsState(listOf())
    val allNews by vm.allNews.observeAsState(listOf())
    val myNews by remember {
        derivedStateOf {
            allNews.filter { myClubs.map { club -> club.ref }.contains(it.clubId) }
        }
    }
    val clubUris by imageVm.clubBannerUris.observeAsState()
    val myClubsUris by remember {
        derivedStateOf {
            clubUris?.let {
                it.filter { pair -> myClubs.map { club -> club.ref }.contains(pair.first) }
            }
        }
    }
    val eventUris by imageVm.eventBannerUris.observeAsState()
    val myEventsUris by remember {
        derivedStateOf {
            eventUris?.let {
                it.filter { pair -> myEvents.map { event -> event.id }.contains(pair.first) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            LazyColumnHeader(text = "My Clubs")
        }
        clubUris?.let { clubUris ->
            if (myClubsUris?.size == myClubs.size) {
                items(myClubs) { club ->
                    val uri = clubUris.find { it.first == club.ref }?.second
                    MyClubTile(
                        club = club,
                        vm = vm,
                        picUri = uri,
                        onClickNews = {
//                        navController.navigate(NavRoutes.ClubAllNewsScreen.route + "/${it.ref}")
                        },
                        onClickUpcoming = {
                            navController.navigate(NavRoutes.EventScreen.route + "/${it}")
                        },
                        onClick = {
                            navController.navigate(NavRoutes.ClubPageScreen.route + "/${club.ref}")
                        }
                    )
                }
            }
        }

        stickyHeader {
            LazyColumnHeader(text = "My Events")
        }
        eventUris?.let { eventUris ->
            if (myEventsUris?.size == myEvents.size) {
                items(myEvents) { event ->
                    val uri = eventUris.find { it.first == event.id }?.second
                    EventTile(
                        event = event,
                        picUri = uri,
                        onClick = {
                            navController.navigate(NavRoutes.EventScreen.route + "/${event.id}")
                        }
                    )
                }
            }
        }

        stickyHeader {
            LazyColumnHeader(
                modifier = Modifier.clickable { navController.navigate(NavRoutes.NewsScreen.route) },
                text = "My News"
            )
        }
        items(myNews) {
            SmallNewsTile(
                news = it
            ) {
                navController.navigate(NavRoutes.SingleNewsScreen.route + "/${it.id}")
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MyClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    picUri: Uri?,
    vm: HomeScreenViewModel,
    onClick: () -> Unit,
    onClickNews: () -> Unit,
    onClickUpcoming: (String) -> Unit
) {
    val allEvents by vm.allEvents.observeAsState(listOf())
    val nextEvent by remember {
        derivedStateOf {
            val clubEvents = allEvents
                .filter { it.clubId == club.ref }
            if (clubEvents.isNotEmpty()) {
                clubEvents.sortedBy { it.date }[0]
            } else {
                null
            }
        }
    }
    val allNews by vm.allNews.observeAsState(listOf())
    val newsAmount by remember {
        derivedStateOf {
            if (allNews.isNotEmpty()) {
                allNews.count { it.clubId == club.ref }
            } else {
                null
            }
        }
    }

    Card(
        modifier = modifier
            .aspectRatio(2.125f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Row(Modifier.fillMaxSize()) {
            AsyncImage(
                modifier = Modifier.aspectRatio(0.75f),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(picUri)
                    .crossfade(true)
                    .build(),
                error = painterResource(id = R.drawable.nokia_logo),
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
                        .background(colorScheme.outlineVariant)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UpcomingEvent(
                        modifier = Modifier.fillMaxHeight(),
                        upcoming = nextEvent,
                        onClick = {
                            nextEvent?.let {
                                onClickUpcoming(it.id)
                            }
                        })
                    Spacer(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(colorScheme.outlineVariant)
                    )
                    NewsIconSection(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f),
                        amount = newsAmount
                    ) {
                        onClickNews()
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
                imageVector = Icons.Outlined.Feed,
                contentDescription = "news"
            )
            if (amount != null) {
                Card(
                    modifier = Modifier.align(Alignment.TopEnd),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(colorScheme.error)
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
fun UpcomingEvent(modifier: Modifier = Modifier, upcoming: Event?, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .aspectRatio(1.45f)
            .clickable { onClick() },
    ) {
        if (upcoming == null) {
            Text(text = "No upcoming events", fontSize = 12.sp, fontWeight = FontWeight.Light)
        } else {
            val sdf = SimpleDateFormat("dd.MM.yyyy", java.util.Locale.ENGLISH)
            val sdfTime = SimpleDateFormat("HH:mm", java.util.Locale.ENGLISH)
            val date = sdf.format(upcoming.date.toDate())
            val time = sdfTime.format(upcoming.date.toDate())
            Text(text = "Upcoming event", fontSize = 12.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = upcoming.name,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        null,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(12.dp)
                    )
                    Text(text = date, fontSize = 12.sp, fontWeight = FontWeight.Light)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        null,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(12.dp)
                    )
                    Text(text = time, fontSize = 12.sp, fontWeight = FontWeight.Light)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FAB(isExpanded: Boolean, navController: NavController, onClick: () -> Unit) {
    val actions = listOf(
        Pair("News") { navController.navigate(NavRoutes.CreateNewsScreen.route) },
        Pair("Event") { navController.navigate(NavRoutes.CreateEvent.route) },
        Pair("Club") { navController.navigate(NavRoutes.CreateClub.route) }
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.End,
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(delayMillis = 0)),
            exit = fadeOut(animationSpec = tween(delayMillis = 0))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                actions.forEachIndexed { index, pair ->
                    FABAction(modifier = Modifier.animateEnterExit(
                        enter = scaleIn(
                            animationSpec = tween(
                                durationMillis = 200,
                                delayMillis = (-index + actions.size - 1) * 20
                            )
                        ),
                        exit = scaleOut(animationSpec = tween(durationMillis = 50))
                    ), text = pair.first, onClick = { pair.second() })
                }
            }
        }
        Card(
            modifier = Modifier
                .size(56.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                colorScheme.surface
            ),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = "add",
                    tint = colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FABAction(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(
            colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}