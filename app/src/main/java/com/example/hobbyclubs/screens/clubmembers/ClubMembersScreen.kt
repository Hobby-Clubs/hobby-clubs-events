package com.example.hobbyclubs.screens.clubmembers

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementSectionTitle
import com.example.hobbyclubs.general.CustomButton

/**
 * Club members screen displays a list of joined members. It is separated by admins and normal members
 *
 * @param navController for Compose navigation
 * @param vm [ClubMembersViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMembersScreen(
    navController: NavController,
    vm: ClubMembersViewModel = viewModel(),
    clubId: String
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfMembers by vm.listOfMembers.observeAsState(listOf())

    // get data of selected club
    LaunchedEffect(Unit) {
        vm.getClub(clubId)
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
                    text = "Club Members",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                ListOfClubMembers(listOfMembers, vm, it)
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
 * List of club members displays a list of all members divided into admins and normal members
 *
 * @param listOfMembers all members that have joined club
 * @param vm [ClubMembersViewModel]
 * @param club Club object fetched from firebase
 */
@Composable
fun ListOfClubMembers(
    listOfMembers: List<User>,
    vm: ClubMembersViewModel,
    club: Club,
) {
    var selectedMemberUid: String? by remember { mutableStateOf(null) }
    val listOfAdmins = listOfMembers.filter { club.admins.contains(it.uid) }
    val listOfNormalMembers = listOfMembers.filter { !club.admins.contains(it.uid) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { ClubManagementSectionTitle(text = "Admins") }
        items(listOfAdmins) { admin ->
            MemberCard(
                user = admin,
                setSelectedMemberUid = {
                    selectedMemberUid = if (selectedMemberUid == admin.uid) null else admin.uid
                },
                onPromote = {
                    vm.promoteToAdmin(clubId = club.ref, admin.uid)
                    selectedMemberUid = null
                },
                isSelected = selectedMemberUid == admin.uid,
                vm = vm,
                club = club,
            )
        }
        item { ClubManagementSectionTitle(text = "Members") }
        items(listOfNormalMembers) { member ->
            MemberCard(
                user = member,
                setSelectedMemberUid = {
                    selectedMemberUid =
                        if (selectedMemberUid == member.uid) null else member.uid
                },
                onPromote = {
                    vm.promoteToAdmin(clubId = club.ref, member.uid)
                    selectedMemberUid = null
                },
                isSelected = selectedMemberUid == member.uid,
                vm = vm,
                club = club,
            )
        }
    }

}

/**
 * Member card is the card that shows request details
 *
 * @param user User object that is fetched from firebase
 * @param setSelectedMemberUid action to do when card is pressed
 * @param onPromote action to do when user has pressed the promote button on the request card
 * @param isSelected when user has pressed the card this becomes true,
 * when user presses another card afterwards it becomes false.
 * @param vm [ClubMembersViewModel]
 * @param club Club object fetched from firebase
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCard(
    user: User,
    setSelectedMemberUid: () -> Unit,
    onPromote: () -> Unit,
    isSelected: Boolean,
    vm: ClubMembersViewModel,
    club: Club,
) {
    var expandedState by remember { mutableStateOf(false) }
    // promotable to admin
    val isPromotable = !club.admins.contains(user.uid)
    // kickable from club (not admin)
    val isKickable = (user.uid != FirebaseHelper.uid && !club.admins.contains(user.uid))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        onClick = {
            setSelectedMemberUid()
            if (isPromotable) {
                expandedState = true
            }
        },
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
                    text = "${user.fName} ${user.lName}",
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
            }
            if (isSelected && expandedState) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (isPromotable) {
                        CustomButton(
                            onClick = {
                                onPromote()
                            },
                            text = "Promote to admin",
                        )
                    }
                    if (isKickable) {
                        CustomButton(
                            onClick = {
                                vm.kickUserFromClub(club.ref, user.uid)
                            },
                            text = "Kick",
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.error,
                                contentColor = colorScheme.onError
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Member image displays members profile picture if they have one, defaults to Nokia logo
 *
 * @param uri The Uri of the members profile picture
 */
@Composable
fun MemberImage(uri: String?) {
    Card(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = Modifier.size(50.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = painterResource(id = R.drawable.nokia_logo)
        )
    }
}
