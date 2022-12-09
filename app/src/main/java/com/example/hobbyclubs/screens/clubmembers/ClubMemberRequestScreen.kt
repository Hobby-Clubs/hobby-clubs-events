package com.example.hobbyclubs.screens.clubmembers

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
import androidx.navigation.NavHostController
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.general.RequestCard
import com.example.hobbyclubs.general.TopBarBackButton
import com.google.firebase.Timestamp

/**
 * Club member request screen for displaying selected clubs join requests
 *
 * @param navController for Compose navigation
 * @param clubId UID for the club you have selected on home or club screen
 * @param vm [ClubMembersViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMemberRequestScreen(
    navController: NavHostController,
    clubId: String,
    vm: ClubMembersViewModel = viewModel()
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfRequests by vm.listOfRequests.observeAsState(listOf())

    // Get all info about selected club
    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getAllJoinRequests(clubId)
    }
    club?.let {
        Scaffold() { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateBottomPadding(), horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Member Requests",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfMemberRequests(listOfRequests, vm, it)
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
 * List of member requests displays a list of requests
 *
 * @param listOfRequests Provided list of a clubs join requests
 * @param vm [ClubMembersViewModel]
 * @param club Club object that was converted from firebase
 */
@Composable
fun ListOfMemberRequests(
    listOfRequests: List<ClubRequest>,
    vm: ClubMembersViewModel,
    club: Club,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(listOfRequests) { request ->
            RequestCard(
                request = request,
                onAccept = {
                    val changeMap = mapOf(
                        Pair("acceptedStatus", true),
                        Pair("timeAccepted", Timestamp.now())
                    )
                    vm.acceptJoinRequest(
                        clubId = club.ref,
                        requestId = request.id,
                        userId = request.userId,
                        changeMapForRequest = changeMap
                    )
                },
                onReject = {
                    vm.declineJoinRequest(club.ref, request.id)
                },
            )
        }
    }
}
