package com.example.hobbyclubs.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Feed
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
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
import com.example.compose.linkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.notifications.InAppNotificationHelper
import com.example.hobbyclubs.notifications.InAppNotificationService
import com.example.hobbyclubs.general.ClubTile
import java.text.SimpleDateFormat

/**
 * Screen which shows the user all relevant information: my clubs, my events, my news and all news.
 * First screen that the user sees (unless it's the first time they open the app)
 *
 * @param navController
 * @param vm
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: HomeScreenViewModel = viewModel(),
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var fabIsExpanded by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val searchInput by vm.searchInput.observeAsState("")
    val focusManager = LocalFocusManager.current
    val isFirst by vm.isFirstTimeUser.observeAsState()
    val notifCount by vm.unreadAmount.observeAsState()

    // Navigates to FirstTimeScreen if it is the first time opening the app
    LaunchedEffect(isFirst) {
        isFirst?.let {
            if (it) {
                navController.navigate(NavRoute.FirstTime.name)
            }
        }
    }

    // Starts the InAppNotificationService if notifications are enabled in the notification settings
    LaunchedEffect(Unit) {
        FirebaseHelper.uid?.let { uid ->
            val settings = InAppNotificationHelper(context).getNotificationSettings()
            if (settings.none { !it.name.contains("REMINDER", true) }) {
                return@LaunchedEffect
            }
            if (!InAppNotificationService.isRunning(context)) {
                InAppNotificationService.start(context, uid)
            }
        }
    }

    // Starts broadcast receiver for InAppNotifications then stops it when the screen is closed
    DisposableEffect(vm) {
        vm.receiveUnreads(context)
        onDispose {
            vm.unregisterReceiver(context)
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
                notificationsIcon = {
                    NotificationsButton(
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(50.dp),
                        notifCount = notifCount
                    ) {
                        navController.navigate(NavRoute.Notifications.name)
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
            HomeScreenContent(
                vm = vm,
                navController = navController,
            )
        } else {
            SearchResults(vm = vm, navController = navController)
        }
    }
}

/**
 * Bell button which has a red dot if there are new notifications. Opens the NotificationScreen
 *
 * @param modifier
 * @param notifCount
 * @param onClick
 * @receiver
 */
@Composable
fun NotificationsButton(modifier: Modifier = Modifier, notifCount: Int?, onClick: () -> Unit) {
    Box(modifier = modifier.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Box {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
            notifCount?.let {
                if (it > 0) {
                    Card(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(10.dp)
                            .align(Alignment.TopEnd),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(colorScheme.error),
                        content = {}
                    )
                }
            }
        }
    }
}

/**
 * Content shown on the HomeScreen when the search bar has no input.
 * Contains: my clubs (all the clubs that the user has joined) in order of upcoming events (the first
 * one is the one that has the nearest upcoming event), my events (events that the user has
 * joined or liked), my news (news that relate to the clubs the user has joined) and all news (all
 * the news on the app). All of those lists are limited to the first 5 elements to limit the size
 * of this screen. If the user wants to access more, they can click on the headers of the sections.
 *
 * @param vm
 * @param navController
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    vm: HomeScreenViewModel,
    navController: NavController,
) {
    val myClubs by vm.myClubs.observeAsState(listOf())
    val myEvents by vm.myEvents.observeAsState(listOf())
    val myNews by vm.myNews.observeAsState(listOf())
    val allNews by vm.allNews.observeAsState(listOf())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            LazyColumnHeader(
                text = "My Clubs",
                onHomeScreen = true,
                onClick = {
                    navController.navigate(NavRoute.AllMy.name + "/club")
                }
            )
        }
        if (myClubs.isNullOrEmpty()) {
            item {
                Column {
                    Text(text = "You have not joined any clubs yet.")
                    Row {
                        Text(text = "Press ")
                        Text(
                            text = "here ",
                            color = linkBlue,
                            modifier = Modifier.clickable {
                                navigateToNewTab(navController, NavRoute.Clubs.name)
                            }
                        )
                        Text(text = "to see new clubs")
                    }
                }
            }
        } else {
            items(myClubs.take(5)) { club ->
                MyClubTile(
                    club = club,
                    vm = vm,
                    onClickNews = {
                        navController.navigate(NavRoute.ClubNews.name + "/true/${club.ref}")
                    },
                    onClickUpcoming = {
                        navController.navigate(NavRoute.Event.name + "/${it}")
                    },
                    onClick = {
                        navController.navigate(NavRoute.ClubPage.name + "/${club.ref}")
                    }
                )
            }
        }

        stickyHeader {
            LazyColumnHeader(
                text = "My Events",
                onHomeScreen = true,
                onClick = { navController.navigate(NavRoute.AllMy.name + "/event") })
        }
        if (myEvents.isNullOrEmpty()) {
            item {
                Column {
                    Text(text = "You have not joined or liked any events yet.")
                    Row {
                        Text(text = "Press ")
                        Text(
                            text = "here ",
                            color = linkBlue,
                            modifier = Modifier.clickable {
                                navigateToNewTab(navController, NavRoute.Calendar.name)
                            }
                        )
                        Text(text = "to see new events")
                    }
                }
            }
        } else {
            items(myEvents.take(5)) { event ->
                EventTile(
                    event = event,
                    onClick = {
                        navController.navigate(NavRoute.Event.name + "/${event.id}")
                    }, navController = navController
                )
            }
        }

        stickyHeader {
            LazyColumnHeader(
                text = "My News",
                onHomeScreen = true,
                onClick = {
                    navController.navigate(NavRoute.AllMy.name + "/news")
                }
            )
        }
        if (myNews.isNullOrEmpty()) {
            item {
                Column {
                    Text(text = "You have not joined any clubs yet.")
                    Row {
                        Text(text = "Press ")
                        Text(
                            text = "here ",
                            color = linkBlue,
                            modifier = Modifier.clickable {
                                navigateToNewTab(navController, NavRoute.Clubs.name)
                            }
                        )
                        Text(text = "to see new clubs")
                    }
                }
            }
        } else {
            items(myNews.take(5)) { singleNews ->
                SmallNewsTile(
                    news = singleNews,
                ) {
                    navController.navigate(NavRoute.SingleNews.name + "/${singleNews.id}")
                }
            }
        }
        stickyHeader {
            LazyColumnHeader(
                text = "All News",
                onHomeScreen = true,
                onClick = {
                    navController.navigate(NavRoute.News.name)
                }
            )
        }
        items(allNews.take(5)) {
            SmallNewsTile(
                news = it
            ) {
                navController.navigate(NavRoute.SingleNews.name + "/${it.id}")
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Results of a search on the HomeScreen when using the top search bar. Shows all the clubs and all
 * the relevant events (from my clubs) which correspond to the query (contain the searched string)
 *
 * @param vm
 * @param navController
 */
@Composable
fun SearchResults(vm: HomeScreenViewModel, navController: NavController) {
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

    // Filters out the clubs that contain the searched string in their name
    val clubsFiltered by remember {
        derivedStateOf {
            if (searchInput.isNotBlank()) {
                allClubs.filter { club -> club.name.contains(searchInput, ignoreCase = true) }
            } else {
                allClubs
            }
        }
    }

    // Filters out the events of my clubs that contain the searched string in their name
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
            items(clubsFiltered) { club ->
                ClubTile(club = club) {
                    navController.navigate(NavRoute.ClubPage.name + "/${club.ref}")
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
            items(eventsFiltered) { event ->
                EventTile(event = event, navController = navController) {
                    navController.navigate(NavRoute.Event.name + "/${event.id}")
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Label that shows that the user is the admin of a club on the MyClubTile
 *
 * @param modifier
 */
@Composable
fun StatusLabel(modifier: Modifier = Modifier, text: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(colorScheme.primary),
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
        )
    }
}

/**
 * Represents a club joined by the current user.
 * Contains the club's banner, name, next upcoming event and number of
 * unread news.
 * Shows an AdminLabel if the user is an admin of that club
 *
 * @param modifier
 * @param club
 * @param vm
 * @param onClick
 * @param onClickNews
 * @param onClickUpcoming
 */
@Composable
fun MyClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    vm: HomeScreenViewModel,
    onClick: () -> Unit,
    onClickNews: () -> Unit,
    onClickUpcoming: (String) -> Unit
) {
    val isAdmin = club.admins.contains(FirebaseHelper.uid)
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
                allNews.count { it.clubId == club.ref && !it.usersRead.contains(FirebaseHelper.uid) }
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
            Box {
                AsyncImage(
                    modifier = Modifier.aspectRatio(0.75f),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(club.bannerUri)
                        .crossfade(true)
                        .build(),
                    error = painterResource(id = R.drawable.nokia_logo),
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop
                )
                if (isAdmin) {
                    StatusLabel(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        text = "Admin"
                    )
                }
            }

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

/**
 * On the MyClubTile, shows the number of unread news.
 * Opens a screen where the user can see those news.
 *
 * @param modifier
 * @param amount
 * @param onClick
 * @receiver
 */
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

/**
 * On the MyClubTile, shows the next upcoming event.
 * Opens the relevant EventScreen
 *
 * @param modifier
 * @param upcoming
 * @param onClick
 * @receiver
 */
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

/**
 * Floating action button which, when expanded, allows the user to create a new club, event or news
 *
 * @param isExpanded
 * @param navController
 * @param onClick
 * @receiver
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FAB(isExpanded: Boolean, navController: NavController, onClick: () -> Unit) {
    val actions = listOf(
        Pair("News") { navController.navigate(NavRoute.CreateNews.name) },
        Pair("Event") { navController.navigate(NavRoute.CreateEvent.name) },
        Pair("Club") { navController.navigate(NavRoute.CreateClub.name) }
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

/**
 * Part of FAB, one of the button that expands when the FAB is clicked
 *
 * @param modifier
 * @param text
 * @param onClick
 */
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