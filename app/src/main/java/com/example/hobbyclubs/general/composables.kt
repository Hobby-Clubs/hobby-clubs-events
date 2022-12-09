package com.example.hobbyclubs.general

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.compose.*
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.*
import com.example.hobbyclubs.navigation.BottomBar
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.notifications.AlarmReceiver
import com.example.hobbyclubs.notifications.EventNotificationInfo
import com.example.hobbyclubs.screens.clubmembers.MemberImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopBar(
    drawerState: DrawerState,
    searchBar: (@Composable () -> Unit)? = null,
    settingsIcon: (@Composable () -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BurgerMenuButton {
            if (drawerState.isClosed) {
                scope.launch {
                    drawerState.open()
                }
            }
        }
        searchBar?.let {
            it()
        }
        settingsIcon?.let {
            it()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(
    modifier: Modifier = Modifier,
    input: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        OutlinedTextField(
            modifier = Modifier
                .width((screenWidth * 0.72).dp)
                .aspectRatio(5.5f)
                .padding(top = 10.dp),
            value = input,
            onValueChange = { onTextChange(it) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "search"
                )
            },
            trailingIcon = {
                if (input.isNotEmpty()) {
                    IconButton(onClick = { onCancel() }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "cancel")
                    }
                }
            },
            singleLine = true,
        )
    }
}

@Composable
fun BurgerMenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Filled.Menu,
            contentDescription = "menu"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    navController: NavController,
    drawerState: DrawerState,
    fab: @Composable () -> Unit = {},
    topBar: @Composable (DrawerState) -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val selectedItem by remember {
        mutableStateOf(0)
    }
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            MockDrawerContent(
                navToFirstTime = { navController.navigate(NavRoutes.FirstTimeScreen.route) },
                logout = {
                    FirebaseHelper.logout()
                    navController.navigate(NavRoutes.LoginScreen.route)
                }) {
                scope.launch {
                    drawerState.close()
                }
            }
        }) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BottomBar(navController = navController) },
            topBar = { topBar(drawerState) },
            floatingActionButton = { fab() }
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockDrawerContent(navToFirstTime: () -> Unit, logout: () -> Unit, onClick: () -> Unit) {
    val items = listOf("Home", "Restaurant", "HobbyClubs", "Parking", "Settings", "Profile")
    val selectedItem = remember { mutableStateOf(items[2]) }
    val context = LocalContext.current

    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item) },
                selected = item == selectedItem.value,
                onClick = {
                    onClick()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
        NavigationDrawerItem(
            label = { Text(text = "First time") },
            selected = false,
            onClick = { navToFirstTime() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(text = "Logout") },
            selected = false,
            onClick = { logout() },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )

        NavigationDrawerItem(
            label = { Text(text = "Test alarm") },
            selected = false,
            onClick = {
                val alarmTime = Calendar.getInstance().timeInMillis + 3000
                val intent = Intent(context, AlarmReceiver::class.java)
                    .apply {
                        val info = EventNotificationInfo(
                            id = 1L,
                            eventId = "9ssrdprFCTrYUeIyoU98",
                            eventTime = alarmTime + (3600000),
                            eventName = "Test event",
                            hoursBefore = 1
                        )
                        putExtra("data", info)
                    }
                val pending = PendingIntent.getBroadcast(
                    context,
                    65786,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                manager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pending
                )
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
fun LazyColumnHeader(
    modifier: Modifier = Modifier,
    text: String,
    onHomeScreen: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.background)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = text,
                fontWeight = FontWeight.Light,
                fontSize = 24.sp
            )
            if (onHomeScreen) Icon(
                Icons.Outlined.NavigateNext,
                null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TopBarBackButton(navController: NavController) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(start = 10.dp)
            .size(30.dp)
            .aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = { navController.navigateUp() }
            ) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PicturePicker(modifier: Modifier = Modifier, uri: Uri?, onPick: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { picUri ->
            picUri?.let {
                onPick(it)
            }
        }
    )
    Card(
        modifier = modifier
            .clickable { launcher.launch("image/*") },
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            model = uri,
            contentDescription = "pic",
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun SwitchPill(
    modifier: Modifier = Modifier,
    isFirstSelected: Boolean,
    onChange: (Boolean) -> Unit,
    firstText: String,
    secondText: String
) {
    Row(modifier = modifier) {
        Pill(modifier = Modifier.weight(1f), isSelected = isFirstSelected, text = firstText) {
            onChange(true)
        }
        Pill(
            modifier = Modifier.weight(1f),
            isLeft = false,
            isSelected = !isFirstSelected,
            text = secondText
        ) {
            onChange(false)
        }
    }
}

@Composable
fun Pill(
    modifier: Modifier = Modifier,
    isLeft: Boolean = true,
    isSelected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
    } else {
        RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
    }

    val color =
        if (isSelected) colorScheme.primary else colorScheme.secondary

    Card(
        shape = shape,
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(color)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Text(modifier = Modifier.padding(8.dp), text = text)
        }
    }
}

@Composable
fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 50.dp)
            .background(color = colorScheme.outlineVariant)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusManager: FocusManager,
    keyboardType: KeyboardType,
    label: String,
    singleLine: Boolean = false,
    placeholder: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType,

            ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        singleLine = singleLine,
        modifier = modifier,
    )
}

@Composable
fun EventTile(
    navController: NavController,
    modifier: Modifier = Modifier,
    event: Event,
    onClick: () -> Unit,
) {

    val joined = event.participants.contains(FirebaseHelper.uid)
    val liked = event.likers.contains(FirebaseHelper.uid)
    val context = LocalContext.current
    var hasRequested: Boolean? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    fun refreshStatus() {
        scope.launch(Dispatchers.IO) {
            hasRequested = getHasRequested(event.id)

        }
    }

    LaunchedEffect(Unit) {
        refreshStatus()
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2.06f)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3.07f)
            ) {
                val uris = if (event.bannerUris.isEmpty()) null else event.bannerUris.first()
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uris)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Tile background",
                    error = painterResource(id = R.drawable.nokia_logo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize(),
                    colorFilter = ColorFilter.tint(Color(0x91000000), BlendMode.Darken)
                )
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row() {
                        hasRequested?.let { hasRequested ->
                            JoinEventButton(
                                isJoined = joined,
                                onJoinEvent = {
                                    if (event.participantLimit != -1) {
                                        if (hasRequested) {
                                            Toast.makeText(
                                                context,
                                                "Request pending approval",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else if (event.isPrivate && event.participants.size < event.participantLimit && !event.admins.contains(
                                                FirebaseHelper.uid
                                            )
                                        ) {
                                            createEventRequest(event, context)
                                            refreshStatus()
                                        } else if (event.participants.size < event.participantLimit) {
                                            joinEvent(event, context)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Event is currently full.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        if (!hasRequested && event.isPrivate && !event.admins.contains(
                                                FirebaseHelper.uid
                                            )
                                        ) {
                                            createEventRequest(event, context)
                                            refreshStatus()
                                        } else {
                                            joinEvent(event, context)
                                        }
                                    }
                                },
                                onLeaveEvent = {
                                    leaveEvent(event, context)
                                },
                                isPrivate = !joined && event.isPrivate && !event.admins.contains(
                                    FirebaseHelper.uid
                                ),
                                requested = hasRequested
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                        if (event.admins.contains(FirebaseHelper.uid)) {
                            ManageEventButton() {
                                navController.navigate(NavRoutes.EventManagementScreen.route + "/${event.id}")
                            }
                        }
                    }

                    if (!joined) {
                        LikeEventButton(isLiked = liked) {
                            likeEvent(event, context)
                        }
                    }

                }
                Text(
                    text = event.name,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    letterSpacing = 0.15.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                val dateFormatted = sdf.format(event.date.toDate())
                val time = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(event.date.toDate())
                EventTileRowItem(
                    modifier = Modifier.weight(1.3f),
                    icon = Icons.Outlined.CalendarMonth,
                    iconDesc = "Calendar Icon",
                    content = dateFormatted
                )
                EventTileRowItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Schedule,
                    iconDesc = "Timer Icon",
                    content = time
                )
                val participants = event.participants.size.toString() +
                        if (event.participantLimit != -1) "/${event.participantLimit}" else ""
                EventTileRowItem(
                    modifier = Modifier.weight(0.6f),
                    icon = Icons.Outlined.People,
                    iconDesc = "People Icon",
                    content = participants
                )
            }
        }
    }
}

@Composable
fun JoinEventButton(
    modifier: Modifier = Modifier,
    isJoined: Boolean,
    isPrivate: Boolean,
    requested: Boolean,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit
) {
    val icon: ImageVector
    val text: String
    if (isJoined) {
        icon = Icons.Outlined.Close
        text = "Cancel"
    } else {
        if (requested) {
            icon = Icons.Outlined.Pending
            text = "Pending.."
        } else if (isPrivate) {
            icon = Icons.Outlined.PersonAddAlt
            text = "Request to join"
        } else {
            icon = Icons.Outlined.PersonAddAlt
            text = "Join"
        }
    }
    var showLeaveEventDialog by remember { mutableStateOf(false) }

    if (showLeaveEventDialog) {
        CustomAlertDialog(
            onDismissRequest = { showLeaveEventDialog = false },
            onConfirm = {
                onLeaveEvent()
                showLeaveEventDialog = false
            },
            title = "Leave event?",
            text = "Are you sure you want to leave this event?",
            confirmText = "Leave"
        )
    }

    Card(
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
        modifier = modifier
            .clickable {
                if (!isJoined) {
                    onJoinEvent()
                } else {
                    showLeaveEventDialog = true
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(horizontal = 16.dp)
                .padding(end = 4.dp)
        ) {
            Icon(
                icon,
                "Join icon",
                tint = colorScheme.onPrimary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(18.dp)
            )
            Text(
                text = text,
                color = colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ManageEventButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = md_theme_light_surfaceTint),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .padding(horizontal = 16.dp)
                .padding(end = 4.dp)
        ) {
            Icon(
                Icons.Outlined.Settings,
                "Settings icon",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(18.dp)
            )
            Text(
                text = "Manage",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LikeEventButton(modifier: Modifier = Modifier, isLiked: Boolean, onClick: () -> Unit) {
    val icon = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Icon(
            icon,
            "Favourite icon",
            tint = colorScheme.tertiary,
            modifier = Modifier
                .padding(4.dp)
                .width(24.dp)
        )
    }
}

@Composable
fun EventTileRowItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconDesc: String,
    content: String
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(modifier = Modifier.size(24.dp), imageVector = icon, contentDescription = iconDesc)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = content, fontSize = 14.sp, color = colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SmallTileForClubManagement(
    modifier: Modifier = Modifier,
    data: Any,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
    val time = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    val isEvent = data is Event
    val title: String
    val date: String
    val picUri: String
    if (isEvent) {
        val event = data as Event
        title = event.name
        date = sdf.format(event.date.toDate()) + " at " + time.format(event.date.toDate())
        picUri = event.bannerUris.first()
    } else {
        val news = data as News
        title = news.headline
        date = sdf.format(news.date.toDate())
        picUri = news.newsImageUri
    }
    Card(
        modifier = modifier
            .aspectRatio(4.7f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(picUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "logo",
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.nokia_logo)
            )
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    modifier = modifier.padding(end = 8.dp),
                    text = date,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = { onDelete() }
            ) {
                Icon(Icons.Outlined.DeleteOutline, null)
            }
        }
    }
}

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmText: String
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = text) },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() },
            ) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
            ) {
                Text(text = confirmText)
            }
        }
    )
}

@Composable
fun SmallNewsTile(
    modifier: Modifier = Modifier,
    news: News,
    onClick: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
    val date = sdf.format(news.date.toDate())

    Card(
        modifier = modifier
            .aspectRatio(4.7f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp)
                .padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(news.clubImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "logo",
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.nokia_logo)
            )
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = news.headline,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width((screenWidth * 0.5).dp)
                    )
                    Text(text = date, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
                Text(
                    modifier = modifier.padding(end = 8.dp),
                    text = news.newsContent,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RequestCard(
    request: ClubRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    var user: User? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {
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
                    MemberImage(uri = it.profilePicUri)
                    Text(
                        text = "${it.fName} ${it.lName}", fontSize = 16.sp, modifier = Modifier
                            .weight(6f)
                            .padding(start = 30.dp)
                    )
                }
                Text(
                    text = request.message, modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                )
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

/**
 * Custom button
 *
 * @param modifier
 * @param onClick
 * @param text
 * @param colors
 * @param icon
 * @receiver
 */
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

@Composable
fun JoinLeaveOrManageButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    type: String
) {
    val text: String = when (type) {
        "join" -> "Join club"
        "leave" -> "Leave club"
        "manage" -> "Manage club"
        "pending" -> "Pending"
        else -> ""
    }
    val icon: ImageVector = when (type) {
        "join" -> Icons.Outlined.PersonAdd
        "leave" -> Icons.Outlined.ExitToApp
        "manage" -> Icons.Outlined.Tune
        "pending" -> Icons.Outlined.Pending
        else -> Icons.Outlined.QuestionMark
    }

    Button(
        modifier = modifier,
        onClick = { onClick() }
    ) {
        Icon(icon, null)
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = text,
        )
    }
}

@Composable
fun CreationPageTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun CreationPageSubtitle(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

/**
 * Page progression displays the current status on the creation pages. Supports 4 horizontal
 * bars that get filled with color corresponding to which page you are on.
 *
 * @param numberOfLines The amount of lines you want to display as filled, maximum 4
 * @param onClick1 changes page to 1
 * @param onClick2 changes page to 2
 * @param onClick3 changes page to 3
 * @param onClick4 changes page to 4
 */
@Composable
fun PageProgression(
    numberOfLines: Int,
    onClick1: () -> Unit,
    onClick2: () -> Unit,
    onClick3: () -> Unit,
    onClick4: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressionBar(isMarked = numberOfLines >= 1, onClick = { onClick1() })
        ProgressionBar(isMarked = numberOfLines > 1, onClick = { onClick2() })
        ProgressionBar(isMarked = numberOfLines > 2, onClick = { onClick3() })
        ProgressionBar(isMarked = numberOfLines > 3, onClick = { onClick4() })
    }
}

/**
 * Progression bar that is used in [PageProgression]
 * @param isMarked if true bar is filled with primary color else show light gray
 * @param onClick action to do when bar is clicked.
 */
@Composable
fun ProgressionBar(isMarked: Boolean, onClick: () -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box(modifier = Modifier
        .width((screenWidth * 0.21).dp)
        .height(13.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(color = if (isMarked) colorScheme.primary else colorScheme.surfaceVariant)
        .clickable { onClick() }
    )
}

/**
 * Selected image item displays an image on the screen. It receives either Bitmap or a Uri.
 * @param uri The uri of an image wanted to be shown on screen
 * @param onDelete Action done when delete is pressed
 */
@Composable
fun SelectedImageItem(uri: Uri? = null, onDelete: () -> Unit) {
    if (uri != null) {
        Box(modifier = Modifier.height(110.dp).width(200.dp)) {
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = Modifier.height(100.dp).width(180.dp).align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
            Card(
                modifier = Modifier.align(Alignment.TopEnd).clickable { onDelete() },
                shape = CircleShape,
                colors = CardDefaults.cardColors(colorScheme.error)
            ) {
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Close, null)
                }

            }
        }
    }
}

/**
 * Club selection dropdown menu for selecting your joined clubs.
 * @param clubList list of clubs you have joined
 * @param onSelect action what happens when user selects the club on the dropdown menu
 */
@Composable
fun ClubSelectionDropdownMenu(clubList: List<Club>, onSelect: (Club) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex: Int? by remember { mutableStateOf(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { expanded = true }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(BorderStroke(1.dp, Color.Black))
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedIndex != null) clubList[selectedIndex!!].name else "Select Club",
                    modifier = Modifier.weight(6f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.weight(1f))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(colorScheme.surface)
        ) {
            clubList.forEachIndexed { index, club ->
                DropdownMenuItem(
                    text = { Text(text = club.name) },
                    onClick = {
                        selectedIndex = index
                        onSelect(club)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Select privacy in either club or event creation.
 *
 * @param selectedPublic when user has tapped on public this will be true otherwise false
 * @param selectedPrivate when user has tapped on private this will be true otherwise false
 * @param onClickPublic action to do when user pressed public button
 * @param onClickPrivate action to do when user pressed private button
 */
@Composable
fun SelectPrivacy(
    selectedPublic: Boolean,
    selectedPrivate: Boolean,
    onClickPublic: () -> Unit,
    onClickPrivate: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Privacy",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Pill(modifier = Modifier.weight(1f), isSelected = selectedPublic, text = "Public") {
                onClickPublic()
            }
            Pill(
                modifier = Modifier.weight(1f),
                isLeft = false,
                isSelected = selectedPrivate,
                text = "Private"
            ) {
                onClickPrivate()
            }
        }
    }
}

/**
 * Club tile displays a card that has the logo, banner and name of the club.
 *
 * @param modifier [Modifier]
 * @param club Club object fetched from firebase
 * @param onClick action to do when tile has been clicked
 */
@Composable
fun ClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    onClick: () -> Unit
) {
    // joined the club displayed in tile
    val isJoined = club.members.contains(FirebaseHelper.uid)
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4.3f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(50.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(club.logoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "logo",
                    error = painterResource(id = R.drawable.nokia_logo),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = club.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = if (isJoined) "Already joined" else "Join now!", fontSize = 14.sp)
                }
            }
            AsyncImage(
                modifier = Modifier.weight(1f),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(club.bannerUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "banner",
                error = painterResource(id = R.drawable.nokia_logo),
                contentScale = ContentScale.Crop
            )
        }
    }
}
