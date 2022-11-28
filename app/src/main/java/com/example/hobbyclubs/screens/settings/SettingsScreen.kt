package com.example.hobbyclubs.screens.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.md_theme_dark_outline
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementSectionTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel(), navController: NavController) {
    val context = LocalContext.current
    val eventSetting by vm.eventSetting.observeAsState(false)
    val newsSetting by vm.newsSetting.observeAsState(false)
    val requestSetting by vm.requestSetting.observeAsState(false)
    var notificationsAllowed by remember { mutableStateOf(false) }
    val hasChanged by remember {
        derivedStateOf {
            val eventPref = vm.getBoolSetting(NotificationSetting.EVENT_NOTIFICATIONS)
            val newsPref = vm.getBoolSetting(NotificationSetting.NEWS_NOTIFICATIONS)
            val requestPref = vm.getBoolSetting(NotificationSetting.REQUEST_NOTIFICATIONS)
            eventSetting != eventPref || newsSetting != newsPref || requestSetting != requestPref
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            notificationsAllowed = it
        }
    )

    LaunchedEffect(Unit) {
        notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
    }

    Scaffold(topBar = {
        SettingsTopBar(
            navController = navController,
            hasChanged = hasChanged,
            onSave = {
                if (!notificationsAllowed) {
                    Toast.makeText(context, "Please allow notifications in settings", Toast.LENGTH_SHORT).show()
                    return@SettingsTopBar
                }
                vm.savePrefs()
                Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                navController.navigateUp()
            })
    }) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
        ) {
            LazyColumn(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Notifications",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
                item {
                    ClubManagementSectionTitle(text = "Events")
                    SettingsSwitchTile(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Event reminders",
                        isActive = eventSetting,
                        onCheckedChange = { isChecked ->
                            vm.updateOption(
                                NotificationSetting.EVENT_NOTIFICATIONS,
                                isChecked
                            )
                        }
                    )
                }

                item {
                    ClubManagementSectionTitle(text = "News")
                    SettingsSwitchTile(
                        icon = Icons.Outlined.Feed,
                        title = "Important news",
                        isActive = newsSetting,
                        onCheckedChange = { isChecked ->
                            vm.updateOption(
                                NotificationSetting.NEWS_NOTIFICATIONS,
                                isChecked
                            )
                        }
                    )
                }

                item {
                    ClubManagementSectionTitle(text = "Clubs")
                    SettingsSwitchTile(
                        icon = Icons.Outlined.PersonAddAlt,
                        title = "Membership requests",
                        isActive = requestSetting,
                        onCheckedChange = { isChecked ->
                            vm.updateOption(
                                NotificationSetting.REQUEST_NOTIFICATIONS,
                                isChecked
                            )
                        }
                    )
                }
            }
        }
    }
}

enum class EventNotificationOption(val hours: Int, val text: String) {
    Start(0, "At the start"),
    Hour(1, "1 hour before"),
    Day(24, "A day before"),
}

enum class NewsNotificationOption(val text: String, val short: String) {
    All("For all news", "All"),
    MyClubs("Only for news related to my clubs", "My clubs"),
    None("None", "None")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(navController: NavController, hasChanged: Boolean = false, onSave: () -> Unit) {
    CenterAlignedTopAppBar(title = { Text(text = "Settings", fontSize = 16.sp) },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (hasChanged) {
                TextButton(onClick = onSave) {
                    Text(text = "SAVE CHANGES")
                }
            }
        }
    )
}

@Composable
fun SettingsSwitchTile(
    icon: ImageVector,
    title: String,
    isActive: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(30.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 30.dp)
            )
            Switch(checked = isActive, onCheckedChange = onCheckedChange)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDropDown(
    icon: ImageVector,
    title: String,
    expandablePart: @Composable () -> Unit,
) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )
    Card(modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(
            animationSpec = tween(
                durationMillis = 300, easing = LinearOutSlowInEasing
            )
        ), onClick = { expandedState = !expandedState }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    null,
                    modifier = Modifier.size(30.dp)
                )
                Text(
                    text = title,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
                Icon(
                    Icons.Outlined.KeyboardArrowDown,
                    "arrow down",
                    modifier = Modifier
                        .weight(1f)
                        .size(30.dp)
                        .rotate(rotationState)
                        .clickable { expandedState = !expandedState }
                )
            }
        }
        if (expandedState) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        md_theme_dark_outline
                    )
            )
            expandablePart()
        }
    }
}

@Composable
fun DropDownTile(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, Modifier.padding(8.dp))
    }
}

//@Composable
//fun DropDownCheckTile(text: String, isChecked: Boolean, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(horizontal = 20.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = Icons.Outlined.CheckCircle,
//            contentDescription = null,
//            tint = if (isChecked) joinedColor else Color.Transparent,
//            modifier = Modifier.padding(end = 16.dp)
//        )
//        Text(text = text)
//    }
//}