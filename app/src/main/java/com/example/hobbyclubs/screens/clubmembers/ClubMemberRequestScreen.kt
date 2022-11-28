package com.example.hobbyclubs.screens.clubmembers

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMemberRequestScreen(
    navController: NavHostController,
    clubId: String,
    vm: ClubMembersViewModel = viewModel()
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfRequests by vm.listOfRequests.observeAsState(listOf())
    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getAllJoinRequests(clubId)
    }
    club?.let { club ->
        Scaffold() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = it.calculateBottomPadding(), horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Member Requests",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfMemberRequests(listOfRequests, vm, club)
            }
            CenterAlignedTopAppBar(
                title = { Text(text = club.name, fontSize = 16.sp) },
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
fun ListOfMemberRequests(listOfRequests: List<Request>, vm: ClubMembersViewModel, club: Club) {
    val context = LocalContext.current


    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(listOfRequests) { request ->
            RequestCard(
                request = request,
                onAccept = {
                    val newlistOfMembers = club.members.toMutableList()
                    newlistOfMembers.add(request.userId)
                    val changeMap = mapOf(
                        Pair("acceptedStatus", true),
                        Pair("timeAccepted", Timestamp.now())
                    )
                    vm.acceptJoinRequest(
                        clubId = club.ref,
                        requestId = request.id,
                        memberListWithNewUser = newlistOfMembers,
                        changeMapForRequest = changeMap
                    )
                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
                },
                onReject = {
                    vm.declineJoinRequest(club.ref, request.id)
                    Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show()
                },
                vm = vm,
            )
        }
    }

}

@Composable
fun RequestCard(
    request: Request,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    vm: ClubMembersViewModel,
) {
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var user: User? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        if (picUri == null) {
            FirebaseHelper.getFile("${CollectionName.users}/${request.userId}")
                .downloadUrl
                .addOnSuccessListener {
                    picUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogoUri", "SmallNewsTile: ", it)
                }
        }
        if (user == null) {
            FirebaseHelper.getUser(request.userId).get()
                .addOnSuccessListener {
                    val fetchedUser = it.toObject(User::class.java)
                    user = fetchedUser
                }
        }
    }
    user?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
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
                        text = "${it.fName} ${it.lName}", fontSize = 16.sp, modifier = Modifier
                            .weight(6f)
                            .padding(start = 30.dp)
                    )
                }
                Text(text = request.message, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp))
                Spacer(modifier = Modifier.height(5.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CustomButton(
                        onClick = {
                            onReject()
                        },
                        text = "Decline",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
                        )
                    )
                    CustomButton(
                        onClick = {
                            onAccept()
                        },
                        text = "Accept",
                    )
                }
            }
        }
    }

}
