package com.example.hobbyclubs.screens.event

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.linkBlue
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.DividerLine
import com.example.hobbyclubs.general.JoinEventButton
import com.example.hobbyclubs.general.LikeEventButton
import com.example.hobbyclubs.screens.clubpage.ClubSectionTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScreen(
    navController: NavController,
    vm: EventScreenViewModel = viewModel(),
    eventId: String
) {
    val context = LocalContext.current
    val event by vm.selectedEvent.observeAsState(null)
    val screenWidth = LocalConfiguration.current.screenWidthDp

    LaunchedEffect(Unit) {
        vm.getEvent(eventId)
    }

    event?.let { club ->
        Box() {
            Scaffold() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(it),
                    horizontalAlignment = Alignment.Start,
                ) {
                    EventHeader(navController, context, club, vm)
                    DividerLine()
                    EventDescription(club.description)
                    DividerLine()
                    EventLocation(club.address)
                    DividerLine()
                    EventLinks(context, club.linkArray)
                    DividerLine()
                    EventContactInfo(club.contactInfoName, club.contactInfoNumber, club.contactInfoEmail)
                }
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventHeader(navController: NavController, context: Context, event: Event, vm: EventScreenViewModel) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val hasJoinedEvent by vm.hasJoinedEvent.observeAsState(false)
    val hasLikedEvent by vm.hasLikedEvent.observeAsState(false)
    val hostClub by vm.selectedEventHostClub.observeAsState(null)
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
    val dateFormatted = sdf.format(event.date.toDate())
    val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(event.date.toDate())

    LaunchedEffect(Unit) {
        if (picUri == null) {
            FirebaseHelper.getAllFiles("${CollectionName.events}/${event.id}")
                .addOnSuccessListener { res ->
                    val items = res.items
                    if (items.isEmpty()) {
                        return@addOnSuccessListener
                    }
                    val bannerRef = items.find { it.name == "0.jpg" } ?: items.first()
                    bannerRef
                        .downloadUrl
                        .addOnSuccessListener {
                            picUri = it
                        }
                        .addOnFailureListener {
                            Log.e("getPicUri", "EventHeader: ", it)
                        }
                }
                .addOnFailureListener {
                    Log.e("getAllFiles", "EventHeader: ", it)
                }
        }
    }


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
                        .data(picUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "background image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight * 0.25).dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                    Text(
                        text = "${event.participants.size} participants",
                        fontSize = 14.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, bottom = 20.dp, top = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                JoinEventButton(isJoined = hasJoinedEvent) {
                    vm.joinEvent(event)
                }
                if (!hasJoinedEvent) {
                    if(!hasLikedEvent) {
                        LikeEventButton(isLiked = hasLikedEvent) {
                            vm.likeEvent(event)
                        }
                    }

                    if(hasLikedEvent) {
                        LikeEventButton(isLiked = hasLikedEvent) {
                            vm.removeLikeOnEvent(event)
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
                EventLinkRow(
                    link = name,
                    onClick = { Toast.makeText(context, name, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun EventLinkRow(link: String, onClick: () -> Unit) {
    Text(
        text = link,
        color = linkBlue,
        modifier = Modifier
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