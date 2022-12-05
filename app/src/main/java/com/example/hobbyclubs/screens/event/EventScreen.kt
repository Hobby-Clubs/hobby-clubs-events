package com.example.hobbyclubs.screens.event

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.linkBlue
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubpage.ClubSectionTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    navController: NavController, vm: EventScreenViewModel = viewModel(), eventId: String
) {
    val context = LocalContext.current
    val selectedEvent by vm.selectedEvent.observeAsState(null)
    val isAdmin by vm.isAdmin.observeAsState(false)

    LaunchedEffect(Unit) {
        vm.getEvent(eventId)
        vm.getEventJoinRequests(eventId)
    }

    selectedEvent?.let { event ->
        Scaffold() { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
                horizontalAlignment = Alignment.Start,
            ) {
                EventHeader(navController, event, isAdmin, vm)
                DividerLine()
                EventDescription(event.description)
                DividerLine()
                EventLocation(event.address)
                DividerLine()
                EventLinks(context, event.linkArray)
                DividerLine()
                EventContactInfo(
                    event.contactInfoName, event.contactInfoNumber, event.contactInfoEmail
                )
            }
            TopAppBar(title = {},
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    TopBarBackButton(navController = navController)
                })
        }
    }
}

@Composable
fun EventHeader(
    navController: NavController, event: Event, isAdmin: Boolean, vm: EventScreenViewModel
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val hasJoinedEvent by vm.hasJoinedEvent.observeAsState(false)
    val hasLikedEvent by vm.hasLikedEvent.observeAsState(false)
    val hostClub by vm.selectedEventHostClub.observeAsState(null)
    val hasRequested by vm.hasRequested.observeAsState(false)
    val context = LocalContext.current

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
    val dateFormatted = sdf.format(event.date.toDate())
    val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(event.date.toDate())

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height((screenHeight * 0.25).dp)

        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(event.bannerUris.first()).crossfade(true).build(),
                    contentDescription = "background image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight * 0.25).dp),
                    contentScale = ContentScale.FillWidth
                )
                if (!hasJoinedEvent) {
                    if (!hasLikedEvent) {
                        LikeEventButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(15.dp),
                            isLiked = hasLikedEvent
                        ) {
                            vm.likeEvent(event)
                        }
                    }

                    if (hasLikedEvent) {
                        LikeEventButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(15.dp),
                            isLiked = hasLikedEvent
                        ) {
                            vm.removeLikeOnEvent(event)
                        }
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.width((screenWidth * 0.5).dp)) {
                    Text(
                        text = event.name,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "$dateFormatted - $time",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier = Modifier.width((screenWidth * 0.5).dp)) {
                    hostClub?.let {
                        Text(
                            text = it.name,
                            fontSize = 18.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Row(modifier = Modifier
                        .clickable {
                            navController.navigate(NavRoutes.EventParticipantsScreen.route + "/${event.id}")
                        }
                        .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End) {
                        Text(
                            text = "${event.participants.size} ${if (event.participants.size == 1) "participant" else "participants"}",
                            fontSize = 14.sp,
                        )
                        Icon(
                            Icons.Filled.NavigateNext,
                            contentDescription = "arrow right",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (hasJoinedEvent) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { vm.leaveEvent(event.id) },
                        ) {
                            Icon(Icons.Outlined.ExitToApp, null)
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = "Cancel",
                            )
                        }
                    }
                    if (!hasJoinedEvent && event.isPrivate && !hasRequested && !isAdmin) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { vm.createEventRequest(event, context) },
                        ) {
                            Icon(Icons.Outlined.PersonAdd, null)
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = "Request",
                            )
                        }
                    }
                    if (!hasJoinedEvent && event.isPrivate && hasRequested) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { },
                        ) {
                            Icon(Icons.Outlined.Pending, null)
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = "Pending",
                            )
                        }
                    }
                    if (!hasJoinedEvent && !event.isPrivate || isAdmin) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { vm.joinEvent(event.id) },
                        ) {
                            Icon(Icons.Outlined.PersonAdd, null)
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = "Join",
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (isAdmin) {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(NavRoutes.EventManagementScreen.route + "/${event.id}") },
                        ) {
                            Icon(Icons.Outlined.Tune, null)
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = "Manage Event",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventDescription(desc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = desc, fontSize = 14.sp)
    }
}

@Composable
fun EventLocation(address: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Location")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = address, fontSize = 14.sp)
    }
}

@Composable
fun EventLinks(context: Context, links: Map<String, String>) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Links")
        Column(verticalArrangement = Arrangement.Top) {
            links.forEach { (name, url) ->
                EventLinkRow(link = name, onClick = {
                    val urlIntent = Intent(
                        Intent.ACTION_VIEW, Uri.parse(url)
                    )
                    context.startActivity(urlIntent)
                })
            }
        }
    }
}

@Composable
fun EventLinkRow(link: String, onClick: () -> Unit) {
    Text(text = link, color = linkBlue, modifier = Modifier
        .clickable { onClick() }
        .padding(5.dp))
}

@Composable
fun EventContactInfo(name: String, phoneNumber: String, email: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Contact Information")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = name)
        Text(text = phoneNumber)
        Text(text = email)
    }
}

@Composable
fun EventTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}