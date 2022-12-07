package com.example.hobbyclubs.screens.clubpage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Club page screen is used to display information about a club selected from home or club screen.
 * it has clubs images, name, description, upcoming events, club news, socials and contact information.
 *
 * @param navController for Compose navigation
 * @param vm [ClubPageViewModel]
 * @param clubId UID for the club you have selected earlier on home or club screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubPageScreen(
    navController: NavController,
    vm: ClubPageViewModel = viewModel(),
    clubId: String
) {
    val context = LocalContext.current
    val selectedClub by vm.selectedClub.observeAsState(null)

    // Get all club related information / events / news / requests
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

/**
 * Club page header displays the clubs:
 * - Banner
 * - Logo
 * - Name
 * - Club members amount
 * - Join / manage button
 * - Share button
 *
 * @param navController for Compose navigation
 * @param context
 * @param club
 * @param vm
 */
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
                .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!hasJoinedClub && !hasRequested) {
                JoinLeaveOrManageButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (clubIsPrivate == true) {
                            showJoinRequestDialog = true
                        } else {
                            vm.joinClub(club.ref)
                        }
                    },
                    type = "join"
                )
            }
            if (!hasJoinedClub && hasRequested) {
                JoinLeaveOrManageButton(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                    type = "pending"
                )
            }
            if (hasJoinedClub) {
                if (isAdmin) {
                    JoinLeaveOrManageButton(
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(NavRoutes.ClubManagementScreen.route + "/${club.ref}") },
                        type = "manage"
                    )
                } else {
                    JoinLeaveOrManageButton(
                        modifier = Modifier.weight(1f),
                        onClick = { vm.leaveClub(clubId = club.ref) },
                        type = "leave"
                    )
                }
            }
            ShareButton(modifier = Modifier.weight(1f), text = "Share", clubId = club.ref)
        }
    }
}

/**
 * Share button opens a share sheet that you can share the club.
 *
 * @param modifier The modifier to be applied to the button.
 * @param text The text that shows on the button.
 * @param clubId UID for the club you have selected earlier on home or club screen
 */
@Composable
fun ShareButton(modifier: Modifier = Modifier, text: String, clubId: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "https://hobbyclubs.fi/clubId=$clubId")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    val context = LocalContext.current

    Button(
        modifier = modifier,
        onClick = {
            context.startActivity(shareIntent)
        }
    ) {
        Icon(Icons.Outlined.Share, null)
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text,
        )
    }
}

/**
 * Club description displays the description of the club.
 *
 * @param desc Value to show on the screen.
 */
@Composable
fun ClubDescription(desc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = desc, fontSize = 14.sp)
    }
}

/**
 * Club schedule displays the selected clubs upcoming events.
 *
 * @param vm [ClubPageViewModel]
 * @param navController for Compose navigation
 */
@Composable
fun ClubSchedule(vm: ClubPageViewModel, navController: NavController) {
    val listOfEvents by vm.listOfEvents.observeAsState(listOf())

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Schedule")
        Text(text = "Upcoming events", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))
        listOfEvents.forEach { event ->
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


/**
 * Club news displays the selected clubs news (up to 5 most recent). When section title
 * has been clicked navigate to another screen that displays all of the clubs news.
 *
 * @param vm [ClubPageViewModel]
 * @param navController for Compose navigation
 * @param clubId UID for the club you have selected earlier on home or club screen
 */
@Composable
fun ClubNews(vm: ClubPageViewModel, navController: NavController, clubId: String) {
    val listOfNews by vm.listOfNews.observeAsState(listOf())

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "News", isNewsTitle = true,
            onClick = { navController.navigate(NavRoutes.ClubNewsScreen.route + "/false/$clubId") })
        Spacer(modifier = Modifier.height(20.dp))
        ClubNewsList(list = listOfNews.take(5), navController = navController)
    }
}

/**
 * Club news list displays the list of news provided.
 *
 * @param list List of News to show
 * @param navController for Compose navigation
 */
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

/**
 * Club links displays the social links of the selected club
 *
 * @param context for starting the intent for opening a link
 * @param linkList clubs social links
 */
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

/**
 * Club link row displays how one row in the link list should look like
 *
 * @param link provided link
 * @param onClick what to do when link has been clicked.
 */
@Composable
fun ClubLinkRow(link: String, onClick: () -> Unit) {
    Text(
        text = link,
        color = linkBlue,
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp))
}

/**
 * Club contact info displays clubs contact persons information
 *
 * @param name contact persons name
 * @param phoneNumber contact persons phone number
 * @param email contact persons email
 */
@Composable
fun ClubContactInfo(name: String, phoneNumber: String, email: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Contact Information")
        Text(text = name)
        Text(text = phoneNumber)
        Text(text = email)
    }
}

/**
 * Club section title is the title for each section of the page.
 *
 * @param text what the dividing title should say
 * @param isNewsTitle show arrow icon on the side indicating it to be clickable
 * @param onClick action what happens on click
 */
@Composable
fun ClubSectionTitle(text: String, isNewsTitle: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        if (isNewsTitle) Icon(
            Icons.Outlined.NavigateNext,
            contentDescription = "",
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Join club dialog is the dialog shown when user tries to join a private club.
 *
 * @param onConfirm action what happens on user pressing send request
 * @param onDismissRequest action when user closes dialog
 * @param vm [ClubPageViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinClubDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    vm: ClubPageViewModel
) {
    val joinClubDialogText by vm.joinClubDialogText.observeAsState(TextFieldValue(""))
    val screenHeight = LocalConfiguration.current.screenHeightDp
    AlertDialog(
        onDismissRequest = {
            // Dismiss the dialog when the user clicks outside the dialog or on the back
            // button. If you want to disable that functionality, simply use an empty
            // onDismissRequest.
            onDismissRequest()
        },
        title = {
            Text(text = "Introduce yourself!")
        },
        text = {
            Column() {
                Text(
                    text = "Please fill in the following form. The admins of the club will review your membership request as soon as possible!"
                )
                OutlinedTextField(
                    modifier = Modifier
                        .height((screenHeight * 0.3).dp)
                        .padding(top = 20.dp),
                    value = joinClubDialogText,
                    onValueChange = { vm.updateDialogText(newVal = it) },
                    label = { Text(text = "Introduction") },
                    placeholder = { Text(text = "Tell us about yourself") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text("Send Request")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}