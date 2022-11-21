package com.example.hobbyclubs.general

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.*
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.CollectionName
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.navigation.BottomBar
import com.example.hobbyclubs.navigation.NavRoutes
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopBar(
    drawerState: DrawerState,
    searchBar: (@Composable () -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
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
                .aspectRatio(4.64f),
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
            bottomBar = { BottomBar(navController) },
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
    }
}

@Composable
fun FakeButtonForNavigationTest(destination: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.padding(10.dp)
    ) {
        Text(text = destination)
    }
}

@Composable
fun LazyColumnHeader(modifier: Modifier = Modifier, text: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = text,
            fontWeight = FontWeight.Light,
            fontSize = 24.sp
        )
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
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

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
            .background(color = Color.Gray)
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
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        modifier = modifier
    )
}

@Composable
fun EventTile(
    modifier: Modifier = Modifier,
    event: Event,
    picUri: Uri?,
    onClick: () -> Unit,
) {
    val joined = event.participants.contains(FirebaseHelper.uid)
    val liked = event.likers.contains(FirebaseHelper.uid)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = clubTileBg),
        border = BorderStroke(1.dp, clubTileBorder),
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
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(picUri)
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
                    JoinEventButton(isJoined = joined) {
                        if (!joined) {
                            joinEvent(event)
                        }
                    }
                    if (!joined) {
                        LikeEventButton(isLiked = liked) {
                            likeEvent(event)
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
                    modifier = Modifier.weight(0.5f),
                    icon = Icons.Outlined.People,
                    iconDesc = "People Icon",
                    content = participants
                )
            }
        }
    }
}

@Composable
fun JoinEventButton(modifier: Modifier = Modifier, isJoined: Boolean, onClick: () -> Unit) {
    val icon = if (isJoined) Icons.Outlined.Check else Icons.Outlined.PersonAddAlt
    val text = if (isJoined) "Joined" else "Join"
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
                icon,
                "Join icon",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(18.dp)
            )
            Text(
                text = text,
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
        colors = CardDefaults.cardColors(containerColor = clubTileBg),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Icon(
            icon,
            "Favourite icon",
            tint = liked,
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
        Text(text = content, fontSize = 14.sp, color = md_theme_light_onSurfaceVariant)
    }
}

@Composable
fun SmallTileForClubManagement(
    modifier: Modifier = Modifier,
    data: Any,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy", java.util.Locale.ENGLISH)
    val time = SimpleDateFormat("HH:mm", java.util.Locale.ENGLISH)
    val isEvent = data is Event
    var title = ""
    var date = ""
    var path = ""
    if (isEvent) {
        val event = data as Event
        title = event.name
        date = sdf.format(event.date.toDate()) + " at " + time.format(event.date.toDate())
        path = "${CollectionName.events}/${event.id}/0.jpg"
    } else {
        val news = data as News
        title = news.headline
        date = sdf.format(news.date.toDate())
        path = "${CollectionName.clubs}/${news.clubId}/logo"
    }
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }
    LaunchedEffect(Unit) {
        if (picUri == null) {
            FirebaseHelper.getFile(path)
                .downloadUrl
                .addOnSuccessListener {
                    picUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogoUri", "SmallNewsTile: ", it)
                }
        }
    }
    Card(
        modifier = modifier
            .aspectRatio(4.7f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(clubTileBg),
        border = BorderStroke(1.dp, clubTileBorder),
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
                Text(text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
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
            Button(onClick = { onDismissRequest() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(text = confirmText)
            }
        }
    )
}

@Composable
fun SmallNewsTile(modifier: Modifier = Modifier, news: News, onClick: () -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var picUri: Uri? by rememberSaveable { mutableStateOf(null) }
    val sdf = SimpleDateFormat("dd.MM.yyyy", java.util.Locale.ENGLISH)
    val date = sdf.format(news.date.toDate())
    LaunchedEffect(Unit) {
        if (picUri == null && news.clubId.isNotEmpty()) {
            FirebaseHelper.getFile("${CollectionName.clubs}/${news.clubId}/logo")
                .downloadUrl
                .addOnSuccessListener {
                    picUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogoUri", "SmallNewsTile: ", it)
                }
        }
    }
    Card(
        modifier = modifier
            .aspectRatio(4.7f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(clubTileBg),
        border = BorderStroke(1.dp, clubTileBorder),
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
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
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