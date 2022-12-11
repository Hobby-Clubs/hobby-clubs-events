package com.example.hobbyclubs.screens.clubmanagement

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
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute

/**
 * Club all events screen displays all the events of the club.
 * You are able to delete events from this screen.
 *
 * @param navController for Compose navigation
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubAllEventsScreen(
    navController: NavController,
    vm: ClubManagementViewModel = viewModel(),
    clubId: String
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfEvents by vm.listOfEvents.observeAsState(null)

    // fetch club data
    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getClubEvents(clubId)
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
                    text = "Club Events",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                listOfEvents?.let {
                    ListOfEvents(
                        list = it,
                        vm = vm,
                        onClick = { eventId ->
                            navController.navigate(NavRoute.Event.name + "/$eventId")
                        }
                    )
                }
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
 * List of events displays a list of selected clubs events that are deletable
 *
 * @param list List of Event objects
 * @param vm [ClubManagementViewModel]
 * @param onClick action to do when user taps on the card instead of the delete button. Used for navigation.
 */
@Composable
fun ListOfEvents(list: List<Event>, vm: ClubManagementViewModel, onClick: (String) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn {
        items(list) { event ->
            SmallTileForClubManagement(
                data = event,
                onClick = {
                    onClick(event.id)
                },
                onDelete = {
                    vm.updateSelection(eventId = event.id)
                    showDeleteDialog = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    if (showDeleteDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteEvent()
                showDeleteDialog = false
            },
            title = "Delete?",
            text = "Are you sure you want to delete? This action cannot be undone.",
            confirmText = "Delete"
        )
    }
}