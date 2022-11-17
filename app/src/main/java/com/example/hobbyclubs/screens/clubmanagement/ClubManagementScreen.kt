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
import com.example.hobbyclubs.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubManagementScreen(
    navController: NavController,
    vm: ClubManagementViewModel = viewModel(),
    clubId: String
) {
    val club by vm.selectedClub.observeAsState(null)

    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getClubEvents(clubId)
        vm.getAllNews(clubId)
    }
    club?.let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Manage club",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                MembersSection(navController, clubId, it)
                Spacer(modifier = Modifier.height(10.dp))
                NewsSection(vm)
                Spacer(modifier = Modifier.height(10.dp))
                EventsSection(vm)
                Spacer(modifier = Modifier.height(10.dp))
                PrivacySection(vm, clubId)
            }
            CenterAlignedTopAppBar(
                title = { Text(text = it.name, fontSize = 16.sp) },
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
fun ClubManagementSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 15.dp)
    )
}

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
                    .weight(1f)
            )
            Text(
                text = title, fontSize = 16.sp, modifier = Modifier
                    .weight(6f)
                    .padding(start = 30.dp)
            )
            if (isMemberRequest) {
                ClubManagementRowNumberCount(
                    modifier = Modifier
                        .weight(1f)
                        .size(30.dp),
                    numberOfItem = numberOfItem,
                    isMemberRequestSection = true
                )
            } else {
                ClubManagementRowNumberCount(
                    modifier = Modifier
                        .weight(1f)
                        .size(30.dp),
                    numberOfItem = numberOfItem
                )
            }

        }
    }
}

@Composable
fun ClubManagementRowNumberCount(
    modifier: Modifier,
    numberOfItem: Int,
    isMemberRequestSection: Boolean = false
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = if (isMemberRequestSection && numberOfItem > 0) Color.Red else Color.Transparent),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = numberOfItem.toString(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 16.sp,
                color = if (isMemberRequestSection && numberOfItem > 0) Color.White else Color.Black,
                maxLines = 1
            )
        }
    }
}

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
                    .background(Color.Gray)
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

@Composable
fun MembersSection(navController: NavController, clubId: String, club: Club) {
    Column() {
        ClubManagementSectionTitle(text = "Members")
        ClubManagementRowCard(
            icon = Icons.Outlined.People,
            iconDesc = "People Icon",
            title = "Members",
            numberOfItem = club.members.size,
            onClick = { navController.navigate(NavRoutes.MembersScreen.route + "/false/$clubId") }
        )
        Spacer(modifier = Modifier.height(10.dp))
        ClubManagementRowCard(
            icon = Icons.Outlined.PersonAddAlt,
            iconDesc = "Person add icon",
            title = "Member requests",
            numberOfItem = 5,
            isMemberRequest = true,
            onClick = { navController.navigate(NavRoutes.MembersScreen.route + "/true/$clubId") }
        )
    }
}

@Composable
fun NewsSection(vm: ClubManagementViewModel) {
    val listOfNews by vm.listOfNews.observeAsState(null)
    Column() {
        ClubManagementSectionTitle(text = "News")
        ClubManagementRowCard(
            icon = Icons.Outlined.Feed,
            iconDesc = "News feed",
            title = "News",
            numberOfItem = listOfNews?.size ?: 0,
            onClick = { }
        )
    }
}

@Composable
fun EventsSection(vm: ClubManagementViewModel) {
    val listOfEvents by vm.listOfEvents.observeAsState(null)
    Column() {
        ClubManagementSectionTitle(text = "Events")
        ClubManagementRowCard(
            icon = Icons.Outlined.CalendarMonth,
            iconDesc = "Calendar",
            title = "Events",
            numberOfItem = listOfEvents?.size ?: 0,
            onClick = { }
        )
    }
}

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

