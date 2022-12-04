package com.example.hobbyclubs.screens.clubpage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.compose.linkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.ClubRequest
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.google.firebase.Timestamp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubPageScreen(
    navController: NavController,
    vm: ClubPageViewModel = viewModel(),
    clubId: String
) {
    val context = LocalContext.current
    val selectedClub by vm.selectedClub.observeAsState(null)

    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getClubEvents(clubId)
        vm.getAllNews(clubId)
        vm.getAllJoinRequests(clubId)
    }
    selectedClub?.let { club ->
        Scaffold {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(it),
                horizontalAlignment = Alignment.Start,
            ) {
                ClubPageHeader(navController, context, club, vm)
                DividerLine()
                ClubDescription(club.description)
                DividerLine()
                ClubSchedule(vm, navController)
                DividerLine()
                ClubNews(vm, navController, clubId)
                DividerLine()
                ClubLinks(context, linkList = club.socials)
                DividerLine()
                ClubContactInfo(
                    name = club.contactPerson,
                    phoneNumber = club.contactPhone,
                    email = club.contactEmail
                )
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

@Composable
fun ClubPageHeader(
    navController: NavController,
    context: Context,
    club: Club,
    vm: ClubPageViewModel
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val hasJoinedClub by vm.hasJoinedClub.observeAsState(false)
    val isAdmin by vm.isAdmin.observeAsState(false)
    val hasRequested by vm.hasRequested.observeAsState(false)
    val clubIsPrivate by vm.clubIsPrivate.observeAsState(null)
    val joinClubDialogText by vm.joinClubDialogText.observeAsState(TextFieldValue(""))
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
        if (showJoinRequestDialog) {
            JoinClubDialog(
                onConfirm = {
                    if (FirebaseHelper.uid != null && joinClubDialogText.text.isNotEmpty()) {
                        val request = ClubRequest(
                            userId = FirebaseHelper.uid!!,
                            acceptedStatus = false,
                            timeAccepted = null,
                            message = joinClubDialogText.text,
                            requestSent = Timestamp.now()
                        )
                        vm.sendJoinClubRequest(clubId = club.ref, request = request)
                        vm.updateUserWithProfilePicUri(FirebaseHelper.uid!!)
                        Toast.makeText(context, "Request sent", Toast.LENGTH_LONG).show()
                        showJoinRequestDialog = false
                    } else {
                        Toast.makeText(context, "Please fill text field", Toast.LENGTH_SHORT).show()
                    }
                },
                onDismissRequest = {
                    showJoinRequestDialog = false
                },
                vm = vm
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = club.bannerUri,
                contentDescription = "background image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.25).dp),
                contentScale = ContentScale.FillWidth
            )
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            ) {
                Text(
                    text = club.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.width((screenWidth * 0.45).dp)
                )
                TextButton(
                    onClick = { navController.navigate(NavRoutes.ClubMembersScreen.route + "/${club.ref}") },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorScheme.onBackground,
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${club.members.size} " + if (club.members.size == 1) "member" else "members")
                        Icon(
                            Icons.Filled.NavigateNext,
                            contentDescription = "arrow right",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(4.0.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                modifier = Modifier.size(150.dp)
            ) {
                AsyncImage(
                    model = club.logoUri,
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.nokia_logo)
                )
            }
            Spacer(modifier = Modifier.width(30.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!hasJoinedClub && clubIsPrivate == true && !hasRequested) {
                CustomButton(
                    text = "Join",
                    onClick = {
                        showJoinRequestDialog = true
                    },
                    icon = Icons.Outlined.PersonAddAlt
                )
            }
            if (clubIsPrivate == true && hasRequested) {
                CustomButton(
                    text = "Pending",
                    onClick = { },
                    icon = Icons.Outlined.Pending
                )
            }
            if (!hasJoinedClub && clubIsPrivate == false) {
                CustomButton(
                    text = "Join",
                    onClick = {
                        vm.joinClub(club.ref)
                    },
                    icon = Icons.Outlined.PersonAddAlt
                )
            }
            if (hasJoinedClub && !isAdmin) {
                CustomButton(
                    text = "Leave club",
                    onClick = {
                        vm.leaveClub(clubId = club.ref)
                    },
                    icon = Icons.Outlined.ExitToApp
                )
            }
            if (hasJoinedClub && isAdmin) {
                CustomButton(
                    text = "Manage club",
                    onClick = {
                        navController.navigate(NavRoutes.ClubManagementScreen.route + "/${club.ref}")
                    }
                )
            }
            ShareButton(text = "Share", clubId = club.ref)
        }
    }
}

@Composable
fun ShareButton(text: String, clubId: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "https://hobbyclubs.fi/clubId=$clubId")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current
    CustomButton(
        onClick = {
            context.startActivity(shareIntent)
        },
        text = text
    )
}

@Composable
fun ClubDescription(desc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = desc, fontSize = 14.sp)
    }
}

@Composable
fun ClubSchedule(vm: ClubPageViewModel, navController: NavController) {
    val listOfEvents by vm.listOfEvents.observeAsState(listOf())

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Schedule")
        Text(text = "Upcoming events", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))
        listOfEvents.forEach { event ->
//            vm.getEventJoinRequests(event.id)
//            val hasRequested by vm.hasRequestedToEvent.observeAsState(false)

            EventTile(
                event = event,
                onClick = {
                    navController.navigate(NavRoutes.EventScreen.route + "/${event.id}")
                }, navController = navController
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}


@Composable
fun ClubNews(vm: ClubPageViewModel, navController: NavController, clubId: String) {
    val listOfNews by vm.listOfNews.observeAsState(listOf())

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "News", isNewsTitle = true,
            onClick = {navController.navigate(NavRoutes.ClubNewsScreen.route + "/false/$clubId")})
        Spacer(modifier = Modifier.height(20.dp))
        ClubNewsList(list = listOfNews.take(5), navController = navController)
    }
}

@Composable
fun ClubNewsList(list: List<News>, navController: NavController) {
        list.forEach { singleNews ->
            SmallNewsTile(
                news = singleNews,
                onClick = {
                    navController.navigate(NavRoutes.SingleNewsScreen.route + "/${singleNews.id}")
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
}

@Composable
fun ClubLinks(context: Context, linkList: Map<String, String>) {

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Links")
        Column(verticalArrangement = Arrangement.Top) {
            linkList.forEach { (name, url) ->
                ClubLinkRow(
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

@Composable
fun ClubLinkRow(link: String, onClick: () -> Unit) {
    Text(
        text = link,
        color = linkBlue,
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp))
}

@Composable
fun ClubContactInfo(name: String, phoneNumber: String, email: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Contact Information")
        Text(text = name)
        Text(text = phoneNumber)
        Text(text = email)
    }
}

@Composable
fun ClubSectionTitle(text: String,isNewsTitle: Boolean = false, onClick: () -> Unit = {}) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(end = 20.dp)
        .clickable { onClick() },
    verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (isNewsTitle) Icon(Icons.Outlined.NavigateNext, contentDescription = "", modifier = Modifier.size(24.dp))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JoinClubDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    vm: ClubPageViewModel
) {
    val joinClubDialogText by vm.joinClubDialogText.observeAsState(TextFieldValue(""))
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .height((screenHeight * 0.55).dp)
                .fillMaxWidth(0.9f)
        ) {
            Box {

                Icon(
                    Icons.Outlined.Close,
                    null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clickable { onDismissRequest() }
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Introduce yourself!",
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Please fill in the following form. The admins of the club will review your membership request as soon as possible!",
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        lineHeight = 15.sp
                    )
                    CustomOutlinedTextField(
                        value = joinClubDialogText,
                        onValueChange = { vm.updateDialogText(newVal = it) },
                        focusManager = focusManager,
                        keyboardType = KeyboardType.Text,
                        label = "Introduction",
                        placeholder = "Tell us about yourself",
                        modifier = Modifier
                            .height((screenHeight * 0.3).dp)
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                    CustomButton(onClick = { onConfirm() }, text = "Send")
                }
            }
        }
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onPrimary,
    ),
    icon: ImageVector? = null
) {
    Button(
        onClick = { onClick() },
        modifier = modifier
            .width(175.dp)
            .height(50.dp)
            .padding(5.dp),
        colors = colors,
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (icon != null) {
                Icon(
                    icon, null, modifier = Modifier
                        .padding(end = 5.dp)
                        .size(14.dp)
                )
            }
            Text(text = text, fontSize = 14.sp)
        }
    }
}