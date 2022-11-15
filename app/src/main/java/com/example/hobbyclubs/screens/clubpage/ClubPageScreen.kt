package com.example.hobbyclubs.screens.clubpage

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.compose.linkBlue
import com.example.compose.nokiaBlue
import com.example.compose.nokiaDarkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.CustomOutlinedTextField
import com.example.hobbyclubs.general.DividerLine
import com.example.hobbyclubs.general.SmallNewsTile
import com.example.hobbyclubs.navigation.NavRoutes
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubPageScreen(
    navController: NavController,
    vm: ClubPageViewModel = viewModel(),
    clubId: String
) {
    val context = LocalContext.current
    val club by vm.selectedClub.observeAsState(null)

    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getLogo(clubId)
        vm.getBanner(clubId)
        vm.getClubEvents(clubId)
        vm.getAllNews(clubId)
    }
    club?.let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                ClubPageHeader(navController, context, it, vm)
                DividerLine()
                ClubDescription(it.description)
                DividerLine()
                ClubSchedule(vm, navController)
                DividerLine()
                ClubNews(vm, navController)
                DividerLine()
                ClubLinks(context, linkList = it.socials)
                DividerLine()
                ClubContactInfo(
                    name = it.contactPerson,
                    phoneNumber = it.contactPhone,
                    email = it.contactEmail
                )
            }
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
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
    val bannerUri by vm.bannerUri.observeAsState()
    val logoUri by vm.logoUri.observeAsState()
    val hasJoinedClub by vm.hasJoinedClub.observeAsState(false)
    val isAdmin by vm.isAdmin.observeAsState(false)
    val clubIsPrivate by vm.clubIsPrivate.observeAsState(null)
    var showJoinRequestDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
        if (showJoinRequestDialog) {
            JoinClubDialog(
                onConfirm = {
                    vm.joinClub(clubId = club.ref)
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
                model = bannerUri,
                contentDescription = "background image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.25).dp),
                contentScale = ContentScale.FillWidth
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            ) {
                Text(
                    text = club.name,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(end = 20.dp)
                )
                TextButton(
                    onClick = { navController.navigate(NavRoutes.MembersScreen.route + "/false/${club.ref}") },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.Transparent
                    )
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
            ClubLogo(modifier = Modifier, logoUri)
            Spacer(modifier = Modifier.width(30.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!hasJoinedClub && clubIsPrivate == true) {
                CustomButton(
                    text = "Join",
                    onClick = {
                        showJoinRequestDialog = true
                    },
                    icon = Icons.Outlined.PersonAddAlt
                )
            }
            if(!hasJoinedClub && clubIsPrivate == false){
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

            CustomButton(
                text = "Share",
                onClick = {
                    Toast.makeText(context, "You are sharing the club", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
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
    val listOfEvents by vm.listOfEvents.observeAsState()
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Schedule")
        Text(text = "Upcoming events", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))
        listOfEvents?.let { events ->
            events.forEach { event ->
                EventTile(vm = vm, event)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


@Composable
fun ClubNews(vm: ClubPageViewModel, navController: NavController) {
    val listOfNews by vm.listOfNews.observeAsState()
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "News")
        Spacer(modifier = Modifier.height(20.dp))
        listOfNews?.let { news ->
            news.forEach { singleNews ->
                SmallNewsTile(
                    news = singleNews,
                    onClick = {
                        // TODO: Navigate to that news page
                    }
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
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
                    onClick = { Toast.makeText(context, name, Toast.LENGTH_SHORT).show() }
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
fun ClubSectionTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
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
fun EventTile(vm: ClubPageViewModel, event: Event) {
    var backgroundUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var selectedEvent: Event? by rememberSaveable { mutableStateOf(null) }
    var joinedEvent: Boolean? by rememberSaveable { mutableStateOf(null) }
    var likedEvent: Boolean? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        if (backgroundUri == null) {
            vm.getEventBackground(event.id)
                .downloadUrl
                .addOnSuccessListener {
                    backgroundUri = it
                }
                .addOnFailureListener {
                    Log.e("getEventBackgroundImage", "EventImageFail: ", it)
                }
        }
        if (selectedEvent == null) {
            vm.getEvent(event.id).get()
                .addOnSuccessListener {
                    val fetchedEvent = it.toObject(Event::class.java)
                    fetchedEvent?.let { event ->
                        Log.d("getEvent", "event: $event")
                        selectedEvent = event
                    }
                }
                .addOnFailureListener {
                    Log.e("getEvent", "getEventFail: ", it)
                }
        }
        if (joinedEvent == null) {
            joinedEvent = event.participants.contains(vm.firebase.uid)
        }
        if (likedEvent == null) {
            likedEvent = event.likers.contains(vm.firebase.uid)
        }
    }
    Box {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                ) {
                    AsyncImage(
                        model = backgroundUri,
                        error = painterResource(id = R.drawable.hockey),
                        contentDescription = "Tile background",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillWidth
                    )
                    Text(
                        text = event.name,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 18.sp,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset.Zero,
                                blurRadius = 5f
                            )
                        )
                    )
                    if (joinedEvent == true) {
                        Card(
                            shape = RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .height(50.dp)
                                .width(110.dp)
                                .padding(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Check,
                                    "Join icon",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .size(16.dp)
                                )
                                Text(text = "Joined", color = Color.White, fontSize = 12.sp)
                            }

                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .height(50.dp)
                                .width(100.dp)
                                .padding(5.dp)
                                .clickable {
                                    vm.joinEvent(event)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.PersonAddAlt,
                                    "Join icon",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(end = 5.dp)
                                        .size(16.dp)
                                )
                                Text(text = "Join", color = Color.White, fontSize = 12.sp)
                            }

                        }
                    }
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .clickable {
                                if (likedEvent == true) {
                                    vm.removeLikeOnEvent(event)
                                } else {
                                    vm.likeEvent(event)
                                }
                            }
                    ) {
                        Icon(
                            if (likedEvent == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            "Favourite icon",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(5.dp)
                                .size(16.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                    val sdf2 = SimpleDateFormat("HH.mm", Locale.ENGLISH)
                    val dateFormatted = sdf.format(event.date.toDate())
                    val timeFormatted = sdf2.format(event.date.toDate())
                    EventTileRowItem(
                        icon = Icons.Outlined.CalendarMonth,
                        iconDesc = "Calendar Icon",
                        content = dateFormatted
                    )
                    EventTileRowItem(
                        icon = Icons.Outlined.Timer,
                        iconDesc = "Timer Icon",
                        content = timeFormatted
                    )
                    EventTileRowItem(
                        icon = Icons.Outlined.People,
                        iconDesc = "People Icon",
                        content = event.participants.size.toString()
                    )
                }
            }
        }
    }
}

@Composable
fun EventTileRowItem(icon: ImageVector, iconDesc: String, content: String) {
    Row() {
        Icon(icon, iconDesc)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = content)
    }
}

@Composable
fun ClubLogo(modifier: Modifier, uri: Uri?) {
    Card(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "avatar",
            modifier = Modifier
                .padding(10.dp)
                .size(125.dp)
        )
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = nokiaDarkBlue,
        contentColor = Color.White,
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