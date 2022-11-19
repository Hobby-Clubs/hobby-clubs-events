package com.example.hobbyclubs.screens.home

import android.net.Uri
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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.clubTileBg
import com.example.compose.clubTileBorder
import com.example.compose.md_theme_light_error
import com.example.compose.md_theme_light_primary
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubs.ClubTile
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, vm: HomeScreenViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val myClubs by vm.myClubs.observeAsState(listOf())
    val isRefreshing by vm.isRefreshing.observeAsState(false)
    val joinedEvents by vm.joinedEvents.observeAsState(listOf())
    val likedEvents by vm.likedEvents.observeAsState(listOf())
    val myEvents by remember {
        derivedStateOf {
            val combined = (joinedEvents + likedEvents).toSet().toList()
            combined.sortedBy { it.date }
        }
    }
    val news by vm.news.observeAsState(listOf())
    var fabIsExpanded by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val searchInput by vm.searchInput.observeAsState("")
    val focusManager = LocalFocusManager.current

    DrawerScreen(
        navController = navController,
        topBar = {
            MenuTopBar(
                drawerState = drawerState
            ) {
                TopSearchBar(
                    modifier = Modifier
                        .fillMaxWidth(),
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
            }
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
                isRefreshing = isRefreshing,
                myClubs = myClubs,
                navController = navController,
                myEvents = myEvents,
                news = news
            )
        } else {
            SearchUI(vm = vm, navController = navController)
        }
    }
}

@Composable
fun SearchUI(vm: HomeScreenViewModel, navController: NavController) {
    val allClubs by vm.allClubs.observeAsState(listOf())
    val searchInput by vm.searchInput.observeAsState("")
    val joinedEvents: List<Event>? by vm.joinedEvents.observeAsState(null)
    val likedEvents: List<Event>? by vm.likedEvents.observeAsState(null)
    val myClubs by vm.myClubs.observeAsState(listOf())
    val clubEvents: List<Event>? by vm.clubEvents.observeAsState(null)
    val myEvents by remember {
        derivedStateOf {
            if (joinedEvents != null && likedEvents != null && clubEvents != null) {
                val combined = (clubEvents!! + joinedEvents!! + likedEvents!!).toSet().toList()
                combined.sortedBy { it.date }
            } else {
                listOf()
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
                myEvents.filter { event -> event.name.contains(searchInput, ignoreCase = true) }
            } else {
                myEvents
            }
        }
    }

    var clubsExpanded by remember { mutableStateOf(true) }
    var eventsExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        vm.fetchAllClubs()
        vm.fetchMyEvents()
    }
    LaunchedEffect(myClubs) {
        if (myClubs.isNotEmpty()) {
            vm.fetchEventsOfMyClubs(myClubs)
        }
    }

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
            item {
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    clubsFiltered.forEach {
                        ClubTile(
                            club = it,
                            logoRef = vm.getLogo(it.ref),
                            bannerRef = vm.getBanner(it.ref)
                        ) {
                            navController.navigate(NavRoutes.ClubPageScreen.route + "/${it.ref}")
                        }
                    }
                }
            }
//            items(clubsFiltered) {
//                ClubTile(
//                    club = it,
//                    logoRef = vm.getLogo(it.ref),
//                    bannerRef = vm.getBanner(it.ref)
//                ) {
//                    navController.navigate(NavRoutes.ClubPageScreen.route + "/${it.ref}")
//                }
//            }
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
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eventsFiltered.forEach {
                        EventTile(event = it) {
                            navController.navigate(NavRoutes.EventScreen.route + "/${it.id}")
                        }
                    }
                }
            }
//            items(eventsFiltered) {
//                Log.d("event", "SearchUI: ${it.id}")
//                EventTile(event = it, onJoin = { }, onLike = { }) {
//
//                }
//            }
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
    isRefreshing: Boolean,
    myClubs: List<Club>,
    navController: NavController,
    myEvents: List<Event>,
    news: List<News>
) {
    SwipeRefresh(
        state = SwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = { vm.refresh() },
        refreshTriggerDistance = 50.dp
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
                MyClubTile(
                    club = it,
                    vm = vm,
                    onClickNews = {
//                        navController.navigate(NavRoutes.ClubAllNewsScreen.route + "/${it.ref}")
                    },
                    onClick = {
                        navController.navigate(NavRoutes.ClubPageScreen.route + "/${it.ref}")
                    }
                )
            }
            stickyHeader {
                LazyColumnHeader(text = "My Events")
            }
            items(myEvents) {
                EventTile(
                    event = it,
                    onClick = {
                        navController.navigate(NavRoutes.EventScreen.route + "/${it.id}")

                    }
                )
            }
            stickyHeader {
                LazyColumnHeader(
                    modifier = Modifier.clickable { navController.navigate(NavRoutes.NewsScreen.route) },
                    text = "My News"
                )
            }
            items(news) {
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
}

@Composable
fun MyClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    vm: HomeScreenViewModel,
    onClick: () -> Unit,
    onClickNews: () -> Unit
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
                    if (news.size == 0) {
                        return@addOnSuccessListener
                    }
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
                model = ImageRequest.Builder(LocalContext.current)
                    .data(picUri)
                    .crossfade(true)
                    .build(),
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
                    UpcomingEvent(
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
                clubTileBg
            ),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = "add",
                    tint = md_theme_light_primary
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
            clubTileBg
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
                color = md_theme_light_primary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
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
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        FakeButtonForNavigationTest(destination = "Home") {
            navController.navigate(NavRoutes.HomeScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Clubs") {
            navController.navigate(NavRoutes.ClubsScreen.route)
        }
        FakeButtonForNavigationTest(destination = "News") {
            navController.navigate(NavRoutes.NewsScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Calendar") {
            navController.navigate(NavRoutes.CalendarScreen.route)
        }
        FakeButtonForNavigationTest(destination = "Create event") {
            navController.navigate(NavRoutes.CreateEvent.route)
        }
        FakeButtonForNavigationTest(destination = "Create club") {
            navController.navigate(NavRoutes.CreateClub.route)
        }
        FakeButtonForNavigationTest(destination = "Log out") {
            scope.launch {
                FirebaseHelper.logout()
                navController.navigate(NavRoutes.LoginScreen.route)
            }
        }
    }
}