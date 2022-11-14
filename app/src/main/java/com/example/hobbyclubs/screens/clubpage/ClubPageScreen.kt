package com.example.hobbyclubs.screens.clubpage

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.compose.linkBlue
import com.example.compose.nokiaBlue
import com.example.compose.nokiaDarkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.general.DividerLine
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
        vm.getCurrentUser()
        vm.getClub(clubId)
        vm.getLogo(clubId)
        vm.getBanner(clubId)
        vm.getClubEvents(clubId)
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
                ClubSchedule(vm)
                DividerLine()
                ClubNews(vm)
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
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
                    onClick = { navController.navigate(NavRoutes.MembersScreen.route + "/false") },
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
            CustomButton(
                text = "Manage Club",
                onClick = {
                    navController.navigate(NavRoutes.ClubManagementScreen.route)
                })
            CustomButton(
                text = "Share",
                onClick = {
                    Toast.makeText(context, "You are sharing the club", Toast.LENGTH_SHORT).show()
                })
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
fun ClubSchedule(vm: ClubPageViewModel) {
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
fun ClubNews(vm: ClubPageViewModel) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "News")
        Spacer(modifier = Modifier.height(20.dp))
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

@Composable
fun EventTile(vm: ClubPageViewModel, event: Event) {
    val joinedEvent by remember { mutableStateOf(false) }
    val currentUser by vm.currentUser.observeAsState()
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
                Image(
                    painter = painterResource(id = R.drawable.hockey),
                    contentDescription = "Tile background",
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
                if (joinedEvent) {
                    Card(
                        shape = RoundedCornerShape(50.dp),
                        colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .height(50.dp)
                            .width(110.dp)
                            .padding(5.dp)
//                            .clickable { vm.joinEvent() }
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
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
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
                val dateFormatted = sdf.format(event.date.toDate())
                EventTileRowItem(
                    icon = Icons.Outlined.CalendarMonth,
                    iconDesc = "Calendar Icon",
                    content = dateFormatted
                )
                EventTileRowItem(
                    icon = Icons.Outlined.Timer,
                    iconDesc = "Timer Icon",
                    content = "19:00"
                )
                EventTileRowItem(
                    icon = Icons.Outlined.People,
                    iconDesc = "People Icon",
                    content = "5"
                )
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
    )
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
        Text(text = text, fontSize = 14.sp)
    }
}