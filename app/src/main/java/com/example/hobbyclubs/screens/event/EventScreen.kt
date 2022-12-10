package com.example.hobbyclubs.screens.event

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Pending
import androidx.compose.material.icons.outlined.PersonAddAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.linkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.screens.clubpage.ClubSectionTitle
import com.example.hobbyclubs.general.CustomButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Event screen for displaying detailed information of a specific event
 *
 * @param navController To manage app navigation within the NavHost
 * @param vm [EventScreenViewModel]
 * @param eventId UID for the specific event displayed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    navController: NavController,
    vm: EventScreenViewModel = viewModel(),
    eventId: String
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
                EventContactInfo(event.contactInfoName, event.contactInfoNumber, event.contactInfoEmail)
            }
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    TopBarBackButton(navController = navController)
                }
            )
        }
    }
}

/**
 * Container for the event's header
 *
 * @param navController To manage app navigation within the NavHost
 * @param event Event object that contains data about the event
 * @param isAdmin Boolean value of whether or not the user is an admin of this specific event
 * @param vm [EventScreenViewModel]
 */
@Composable
fun EventHeader(
    navController: NavController,
    event: Event,
    isAdmin: Boolean,
    vm: EventScreenViewModel
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
                val uris = if(event.bannerUris.isEmpty()) null else event.bannerUris.first()
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uris)
                        .crossfade(true)
                        .build(),
                    contentDescription = "event background image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight * 0.25).dp),
                    contentScale = ContentScale.FillWidth,
                    error = painterResource(id = R.drawable.nokia_logo)
                )
                if (!hasJoinedEvent) {
                    LikeEventButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(15.dp),
                        isLiked = hasLikedEvent
                    ) {
                        updateLikeEvent(event = event, context = context)
                    }
                }
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
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
                        Text(
                            text = if (hostClub != null) hostClub!!.name else "Nokia",
                            fontSize = 18.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                    Row(
                        modifier = Modifier
                            .clickable {
                                navController.navigate(NavRoute.EventParticipants.name + "/${event.id}")
                            }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
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
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasJoinedEvent) {
                    CustomButton(
                        text = "Cancel",
                        onClick = {
                            leaveEvent(event, context)
                        },
                        icon = Icons.Outlined.ExitToApp
                    )
                }
                if (!hasJoinedEvent && event.isPrivate && !hasRequested && !isAdmin) {
                    CustomButton(
                        text = "Request",
                        onClick = {
                            vm.createEventRequest(event, context)
                        },
                        icon = Icons.Outlined.PersonAddAlt
                    )
                }
                if (!hasJoinedEvent && event.isPrivate && hasRequested) {
                    CustomButton(
                        text = "Pending",
                        onClick = { },
                        icon = Icons.Outlined.Pending
                    )
                }
                if (!hasJoinedEvent && (!event.isPrivate || isAdmin)) {
                    CustomButton(
                        text = "Join",
                        onClick = {
                            joinEvent(event, context)
                        },
                        icon = Icons.Outlined.PersonAddAlt
                    )
                }
                if (isAdmin) {
                    CustomButton(
                        text = "Manage Event",
                        onClick = {
                            navController.navigate(NavRoute.EventManagement.name + "/${event.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Container for the event's description
 *
 * @param desc Description string
 */
@Composable
fun EventDescription(desc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = desc, fontSize = 14.sp)
    }
}

/**
 * Container for the event's location
 *
 * @param address Address string
 */
@Composable
fun EventLocation(address: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Location")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = address, fontSize = 14.sp)
    }
}

/**
 * Container for the event's list of links
 *
 * @param context LocalContext
 * @param links Map of strings, links and their names
 */
@Composable
fun EventLinks(context: Context, links: Map<String, String>) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Links")
        Column(verticalArrangement = Arrangement.Top) {
            links.forEach { (name, url) ->
                EventLinkRow(
                    link = name,
                    onClick = {
                        val urlIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                        )
                        context.startActivity(urlIntent)
                    }
                )
            }
        }
    }
}

/**
 * Container for a singular link related to the event
 *
 * @param link Link string
 * @param onClick onClick function
 * @receiver
 */
@Composable
fun EventLinkRow(link: String, onClick: () -> Unit) {
    Text(
        text = link,
        color = linkBlue,
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp))
}

/**
 * Container for the event's contact information
 *
 * @param name Name String
 * @param phoneNumber Phone number string
 * @param email Email string
 */
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

/**
 * Container for a title displayed on the event screen
 *
 * @param text Title string
 */
@Composable
fun EventTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}