package com.example.hobbyclubs.screens.eventparticipants

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.screens.clubmembers.MemberImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventParticipantsScreen(
    navController: NavController,
    vm: EventParticipantsViewModel = viewModel(),
    eventId: String
) {
    val event by vm.selectedEvent.observeAsState(null)
    val listOfParticipants by vm.listOfParticipants.observeAsState(listOf())

    LaunchedEffect(Unit) {
        vm.getEvent(eventId)
    }

    event?. let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Event participants",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfEventParticipants(listOfParticipants, vm, it)
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
fun ListOfEventParticipants(
    participants: List<User>,
    vm: EventParticipantsViewModel,
    event: Event
) {
    val listOfAdmins = participants.filter { event.admins.contains(it.uid) }
    val listOfParticipants = participants.filter { !event.admins.contains(it.uid) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(
                text = "Admins",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
        items(listOfAdmins) { admin ->
            ParticipantCard(user = admin)
        }
        item {
            Text(
                text = "Participants",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 15.dp)
            )
        }
        items(listOfParticipants) { participant ->
            ParticipantCard(user = participant)
        }
    }
}

@Composable
fun ParticipantCard(
    user: User,
) {
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (picUri == null) {
            FirebaseHelper.getFile("${CollectionName.users}/${user.uid}")
                .downloadUrl
                .addOnSuccessListener {
                    picUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogoUri", "SmallNewsTile: ", it)
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberImage(uri = picUri)
                Text(
                    text = "${user.fName} ${user.lName}", fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
            }

        }
    }
}