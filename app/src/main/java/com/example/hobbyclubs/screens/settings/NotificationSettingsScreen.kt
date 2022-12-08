package com.example.hobbyclubs.screens.settings

import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.md_theme_dark_outline
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.screens.clubmanagement.ClubManagementSectionTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationSettingsScreen(
    vm: NotificationSettingsViewModel = viewModel(),
    navController: NavController
) {

    val context = LocalContext.current
    var notificationsAllowed by remember { mutableStateOf(false) }
    val settingValues by vm.settingValues.observeAsState(listOf())
    val retrieved by vm.retrievedSettings.observeAsState(listOf())
    val hasChanged by remember {
        derivedStateOf { settingValues != retrieved }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            notificationsAllowed = it
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        } else {
            notificationsAllowed = true
        }
    }

    Scaffold(
        topBar = {
        SettingsTopBar(
            navController = navController,
            showSave = hasChanged,
            onSave = {
                if (!notificationsAllowed) {
                    Toast.makeText(
                        context,
                        "Please allow notifications in settings",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@SettingsTopBar
                }
                vm.onSave(settingValues)
                Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
            }
        )
    }) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
        ) {
            SettingList(
                settings = settingValues,
                onCheckedChange = { setting, isActive ->
                    vm.changeSetting(settingValues, setting.name, isActive)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(navController: NavController, showSave: Boolean = false, onSave: () -> Unit) {
    CenterAlignedTopAppBar(title = { Text(text = "Notification settings", fontSize = 16.sp) },
        colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            TopBarBackButton(navController = navController)
        },
        actions = {
            if (showSave) {
                TextButton(onClick = onSave) {
                    Text(text = "Save")
                }
            }
        }
    )
}

@Composable
fun SettingsSwitchTile(
    data: NotificationSetting,
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
            Icon(data.icon, null, modifier = Modifier.size(30.dp))
            Text(
                text = data.title,
                fontSize = 14.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 30.dp)
            )
            Switch(checked = isActive, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingList(
    settings: List<Pair<String, Boolean>>,
    onCheckedChange: (NotificationSetting, Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingCategory.values().forEach { category ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClubManagementSectionTitle(text = category.name)
                    NotificationSetting.values()
                        .filter { it.category == category }
                        .forEach { data ->
                            SettingsSwitchTile(
                                data = data,
                                isActive = settings.find { it.first == data.name }?.second ?: false,
                                onCheckedChange = { onCheckedChange(data, it) })
                        }
                }
            }
        }
    }
}

enum class SettingCategory {
    Events, News, Requests
}

enum class NotificationSetting(
    val title: String,
    val category: SettingCategory,
    val icon: ImageVector,
    var isActive: Boolean
) {
    EVENT_NEW(
        title = "New event in my clubs",
        category = SettingCategory.Events,
        icon = Icons.Outlined.NewReleases,
        isActive = false
    ),
    EVENT_HOUR_REMINDER(
        title = "1-hour reminder",
        category = SettingCategory.Events,
        icon = Icons.Outlined.Alarm,
        isActive = false
    ),
    EVENT_DAY_REMINDER(
        title = "1-day reminder",
        category = SettingCategory.Events,
        icon = Icons.Outlined.CalendarToday,
        isActive = false
    ),
    NEWS_GENERAL(
        title = "General news",
        category = SettingCategory.News,
        icon = Icons.Outlined.Feed,
        isActive = false
    ),
    NEWS_CLUB(
        title = "Club news",
        category = SettingCategory.News,
        icon = Icons.Outlined.Feed,
        isActive = false
    ),
    REQUEST_MEMBERSHIP(
        title = "Membership",
        category = SettingCategory.Requests,
        icon = Icons.Outlined.PersonAdd,
        isActive = false
    ),
    REQUEST_MEMBERSHIP_ACCEPTED(
        title = "Accepted membership",
        category = SettingCategory.Requests,
        icon = Icons.Outlined.Check,
        isActive = false
    ),
    REQUEST_PARTICIPATION(
        title = "Event participation",
        category = SettingCategory.Requests,
        icon = Icons.Outlined.PersonAdd,
        isActive = false
    ),
    REQUEST_PARTICIPATION_ACCEPTED(
        title = "Accepted participation",
        category = SettingCategory.Requests,
        icon = Icons.Outlined.Check,
        isActive = false
    ),
}