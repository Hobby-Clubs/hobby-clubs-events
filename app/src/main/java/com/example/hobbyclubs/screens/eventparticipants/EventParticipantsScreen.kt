package com.example.hobbyclubs.screens.eventparticipants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.screens.clubmembers.MemberImage

/**
 * Event participants screen for displaying a list of the event's participants and admins
 *
 * @param navController To manage app navigation within the NavHost
 * @param vm [EventParticipantsViewModel]
 * @param eventId UID for the specific event
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventParticipantsScreen(
    navController: NavController,
    vm: EventParticipantsViewModel = viewModel(),
    eventId: String
) {
    val event by vm.selectedEvent.observeAsState(null)
    val listOfParticipants by vm.listOfParticipants.observeAsState(listOf())
    val listOfAdmins by vm.listOfAdmins.observeAsState(listOf())

    LaunchedEffect(Unit) {
        vm.getEvent(eventId)
    }

    event?.let {
        Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateBottomPadding(), horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Event participants",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfEventParticipants(listOfParticipants, listOfAdmins, it)
            }
            CenterAlignedTopAppBar(
                title = { Text(text = it.name, fontSize = 16.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    TopBarBackButton(navController = navController)
                }
            )
        }
    }
}

/**
 * List of the event's participants
 *
 * @param participants List of participants
 * @param listOfAdmins List of admins
 * @param event Event object
 */
@Composable
fun ListOfEventParticipants(
    participants: List<User>,
    listOfAdmins: List<User>,
    event: Event
) {
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

/**
 * Card for a participant of the event
 *
 * @param user User object
 */
@Composable
fun ParticipantCard(
    user: User,
) {
    ElevatedCard(
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
                MemberImage(uri = user.profilePicUri)
                Text(
                    text = "${user.fName} ${user.lName}", fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
            }

        }
    }
}