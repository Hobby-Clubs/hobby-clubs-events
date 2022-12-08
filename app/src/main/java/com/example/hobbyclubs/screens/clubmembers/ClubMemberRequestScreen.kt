package com.example.hobbyclubs.screens.clubmembers

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.general.RequestCard
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMemberRequestScreen(
    navController: NavController,
    clubId: String,
    vm: ClubMembersViewModel = viewModel()
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfRequests by vm.listOfRequests.observeAsState(listOf())
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

@Composable
fun ListOfMemberRequests(
    listOfRequests: List<ClubRequest>,
    vm: ClubMembersViewModel,
    club: Club,
) {
    val context = LocalContext.current

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
                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
                },
                onReject = {
                    vm.declineJoinRequest(club.ref, request.id)
                    Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
