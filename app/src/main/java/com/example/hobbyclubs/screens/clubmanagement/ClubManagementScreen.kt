package com.example.hobbyclubs.screens.clubmanagement

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.navigation.NavRoute

/**
 * Club management screen for managing the clubs members, events, news and privacy
 *
 * @param navController for Compose navigation
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubManagementScreen(
    navController: NavController,
    vm: ClubManagementViewModel = viewModel(),
    clubId: String
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfRequests by vm.listOfRequests.observeAsState(listOf())

    // get all data needed for managing club
    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getClubEvents(clubId)
        vm.getAllNews(clubId)
        vm.getAllJoinRequests(clubId)
    }
    club?.let {
        Scaffold() { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = padding.calculateBottomPadding()),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Manage club",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                MembersSection(navController, clubId, it, listOfRequests.size)
                Spacer(modifier = Modifier.height(10.dp))
                NewsSection(navController, vm, clubId)
                Spacer(modifier = Modifier.height(10.dp))
                EventsSection(navController, vm, clubId)
                Spacer(modifier = Modifier.height(10.dp))
                PrivacySection(vm, clubId)
            }
            CenterAlignedTopAppBar(
                title = { Text(text = it.name, fontSize = 16.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { navController.navigate(NavRoute.ClubSettings.name + "/$clubId") }) {
                        Icon(Icons.Outlined.Settings, null, modifier = Modifier.padding(end = 10.dp))
                    }
                },
                navigationIcon = {
                    TopBarBackButton(navController = navController)
                }
            )
        }
    }
}

/**
 * Club management section title
 * @param text Text to be shown as title
 */
@Composable
fun ClubManagementSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 15.dp)
    )
}

/**
 * Club management row card showing the icon, title, and value how many of certain type.
 *
 * @param icon Icon to display on the left of the card
 * @param iconDesc Description of icon
 * @param title Text on the card
 * @param numberOfItem How many items of certain type
 * @param isMemberRequest if member request card, show red notification circle around the number of item.
 * @param onClick action to do when user taps on the card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubManagementRowCard(
    icon: ImageVector,
    iconDesc: String,
    title: String,
    numberOfItem: Int,
    isMemberRequest: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon, iconDesc, modifier = Modifier
                    .size(30.dp)
            )
            Text(
                text = title, fontSize = 16.sp, modifier = Modifier
                    .weight(6f)
                    .padding(start = 30.dp)
            )
            if (isMemberRequest) {
                ClubManagementRowNumberCount(
                    modifier = Modifier
                        .size(24.dp),
                    numberOfItem = numberOfItem,
                    isMemberRequestSection = true
                )
            } else {
                ClubManagementRowNumberCount(
                    modifier = Modifier
                        .size(24.dp),
                    numberOfItem = numberOfItem
                )
            }
        }
    }
}

/**
 * Club management row number count
 *
 * @param modifier [Modifier]
 * @param numberOfItem number of item
 * @param isMemberRequestSection if member request card, show red notification circle around the number of item.
 */
@Composable
fun ClubManagementRowNumberCount(
    modifier: Modifier,
    numberOfItem: Int,
    isMemberRequestSection: Boolean = false
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = if (isMemberRequestSection && numberOfItem > 0) colorScheme.error else Color.Transparent),
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = numberOfItem.toString(),
                fontSize = 16.sp,
                color = if (isMemberRequestSection && numberOfItem > 0) colorScheme.onError else colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Expandable privacy card for selecting private or public club.
 *
 * @param icon Icon to display
 * @param iconDesc Description of icon
 * @param title Text to show on card
 * @param clubId UID for the club you have selected on home or club screen
 * @param vm [ClubManagementViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandablePrivacyCard(
    icon: ImageVector,
    iconDesc: String,
    title: String,
    clubId: String,
    vm: ClubManagementViewModel,
) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        onClick = { expandedState = !expandedState }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, iconDesc, modifier = Modifier
                        .size(30.dp)
                        .weight(1f)
                )
                Text(
                    text = title, fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
                Icon(
                    Icons.Outlined.KeyboardArrowDown, "arrow down", modifier = Modifier
                        .weight(1f)
                        .size(30.dp)
                        .rotate(rotationState)
                )
            }
        }
        if (expandedState) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorScheme.outlineVariant)
                    .height(1.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            PrivacyRowItem(
                icon = Icons.Outlined.Public,
                iconDesc = "Public", title = "Public",
                onClick = {
                    vm.updatePrivacy(false, clubId = clubId)
                    expandedState = false
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
            PrivacyRowItem(
                icon = Icons.Outlined.Lock,
                iconDesc = "Private", title = "Private",
                onClick = {
                    vm.updatePrivacy(true, clubId = clubId)
                    expandedState = false
                }
            )
        }
    }
}

/**
 * Privacy row item shows in the privacy dropdown menu
 *
 * @param icon Icon to be shown
 * @param iconDesc Description of icon
 * @param title Text to be shown on card
 * @param onClick action to do when user taps on the row item.
 */
@Composable
fun PrivacyRowItem(
    icon: ImageVector,
    iconDesc: String,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 10.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, iconDesc, modifier = Modifier
                .size(30.dp)
                .weight(1f)
        )
        Text(
            text = title, fontSize = 16.sp, modifier = Modifier
                .weight(6f)
                .padding(start = 30.dp)
        )
    }
}

/**
 * Members section includes Members and Member requests cards
 *
 * @param navController for Compose navigation
 * @param clubId UID for the club you have selected on home or club screen
 * @param club Club object fetched from firebase
 * @param requestsAmount amount of join requests
 */
@Composable
fun MembersSection(navController: NavController, clubId: String, club: Club, requestsAmount: Int) {
    Column() {
        ClubManagementSectionTitle(text = "Members")
        ClubManagementRowCard(
            icon = Icons.Outlined.People,
            iconDesc = "People Icon",
            title = "Members",
            numberOfItem = club.members.size,
            onClick = { navController.navigate(NavRoute.ClubMembers.name + "/$clubId") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ClubManagementRowCard(
            icon = Icons.Outlined.PersonAddAlt,
            iconDesc = "Person add icon",
            title = "Member requests",
            numberOfItem = requestsAmount,
            isMemberRequest = true,
            onClick = { navController.navigate(NavRoute.ClubMemberRequest.name + "/$clubId") }
        )
    }
}

/**
 * News section to show news card
 *
 * @param navController for Compose navigation
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@Composable
fun NewsSection(navController: NavController, vm: ClubManagementViewModel, clubId: String) {
    val listOfNews by vm.listOfNews.observeAsState(null)
    Column() {
        ClubManagementSectionTitle(text = "News")
        ClubManagementRowCard(
            icon = Icons.Outlined.Feed,
            iconDesc = "News feed",
            title = "News",
            numberOfItem = listOfNews?.size ?: 0,
            onClick = { navController.navigate(NavRoute.ClubAllNews.name + "/$clubId") }
        )
    }
}

/**
 * Events section to show event card
 *
 * @param navController for Compose navigation
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@Composable
fun EventsSection(navController: NavController, vm: ClubManagementViewModel, clubId: String) {
    val listOfEvents by vm.listOfEvents.observeAsState(null)
    Column() {
        ClubManagementSectionTitle(text = "Events")
        ClubManagementRowCard(
            icon = Icons.Outlined.CalendarMonth,
            iconDesc = "Calendar",
            title = "Events",
            numberOfItem = listOfEvents?.size ?: 0,
            onClick = { navController.navigate(NavRoute.ClubAllEvents.name + "/$clubId") }
        )
    }
}

/**
 * Privacy section to change clubs privacy
 *
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@Composable
fun PrivacySection(vm: ClubManagementViewModel, clubId: String) {
    val isPrivate by vm.isPrivate.observeAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        ClubManagementSectionTitle(text = "Privacy")
        ExpandablePrivacyCard(
            icon = if (isPrivate == true) Icons.Outlined.Lock else Icons.Outlined.Public,
            iconDesc = "privacy",
            title = if (isPrivate == true) "Private" else "Public",
            vm = vm,
            clubId = clubId
        )
    }
}

