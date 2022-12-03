package com.example.hobbyclubs.screens.notifications

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.md_theme_light_primary
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.NotificationInfo
import com.example.hobbyclubs.api.NotificationType
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.general.toString
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.notifications.NotificationContent
import com.example.hobbyclubs.screens.settings.NotificationSetting
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    vm: NotificationScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val unreads by vm.unreads.observeAsState()
    val isRefreshing by vm.isRefreshing.observeAsState(false)

    DisposableEffect(vm) {
        vm.receiveUnreads(context)
        onDispose {
            vm.unregisterReceiver(context)
        }
    }

    Scaffold(
        topBar = {
            NotificationsTopBar(
                navController = navController,
                onClickSettings = { navController.navigate(NavRoutes.SettingsScreen.route) })
        },
        floatingActionButton = {
            Column() {
                FloatingActionButton(onClick = { vm.removeRead() }) {
                    Icon(imageVector = Icons.Outlined.Undo, contentDescription = null)
                }
                Spacer(modifier = Modifier.size(8.dp))
                FloatingActionButton(onClick = {
                    val notif = NotificationInfo(
                        type = NotificationType.NEWS_GENERAL.name,
                        newsId = "rMULEun8hJWYewpG3z4R"
                    )
                    FirebaseHelper.addNotification(notif)
                }) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                }
            }

        }
    ) { pad ->
        Box(modifier = Modifier.padding(pad)) {
            if (unreads == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Loading notifications")
                    }

                }
            } else {
                unreads?.let { data ->
                    if (data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No new notifications")
                        }
                    } else {
                        SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                            onRefresh = { vm.refresh() },
                            refreshTriggerDistance = 50.dp
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(data, { item -> item.id }) {
                                    NotificationTile(
                                        content = it,
                                        onDismiss = { id ->
                                            vm.markAsRead(id)
                                        },
                                        onClick = {
                                            it.navRoute?.let { route ->
                                                navController.navigate(route)
                                            }
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTopBar(navController: NavController, onClickSettings: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(text = "Notifications") },
        navigationIcon = {
            TopBarBackButton(navController = navController)
        },
        actions = {
            IconButton(onClick = onClickSettings, content = {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "settings"
                )
            })
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotificationTile(
    content: NotificationContent,
    onDismiss: (String) -> Unit,
    onClick: () -> Unit,
) {

    val dismissState = rememberDismissState()

    if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        Log.d("markAsSeen", "NotificationScreen: ${content.id}")
        onDismiss(content.id)
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd
        ),
        background = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.Transparent
                    else -> MaterialTheme.colors.error
                }
            )
            val alignment = Alignment.CenterStart
            val icon = Icons.Default.Delete

            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
            )
            Card(
                Modifier
                    .fillMaxSize(),
                colors = CardDefaults.cardColors(color)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        icon,
                        contentDescription = "Delete Icon",
                        modifier = Modifier.scale(scale),
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        }
    ) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                content.setting?.icon?.let {
                    val color = when (content.setting) {
                        NotificationSetting.EVENT_NEW -> MaterialTheme.colors.error
                        NotificationSetting.EVENT_HOUR_REMINDER -> Color.White
                        NotificationSetting.EVENT_DAY_REMINDER -> Color.White
                        NotificationSetting.NEWS_GENERAL -> Color.Black
                        NotificationSetting.NEWS_CLUB -> Color.Black
                        NotificationSetting.REQUEST_MEMBERSHIP -> md_theme_light_primary
                        NotificationSetting.REQUEST_ACCEPTED -> Color.Green
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = it, contentDescription = null, tint = Color.White)
                    }
                }
                Column() {
                    Text(text = content.title.trim(), fontWeight = FontWeight.SemiBold)
                    Text(text = content.content.trim(), fontSize = 12.sp, lineHeight = 15.sp)
                    Text(
                        text = getDateText(content.date),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

fun getDateText(notificationDate: Date): String {
    val now = Date()
    val yearNow = now.toString("yyyy")
    val yearNotif = notificationDate.toString("yyyy")
    val timeString = notificationDate.toString("HH:mm")
    val dayOfWeek = notificationDate.toString("EEEE")
    val dateSameYear = notificationDate.toString("EEE dd")
    val dateOtherYear = notificationDate.toString("EEE dd yyyy")
    val dayInMillis = 86400000L
    val weekInMillis = 7 * dayInMillis
    val timeDiff = now.time - notificationDate.time
    val isToday =
        now.toString("dd.MM.yyyy") == notificationDate.toString("dd.MM.yyyy")

    if (isToday) {
        return "Today at $timeString"
    }
    if (timeDiff < dayInMillis) {
        return "Yesterday at $timeString"
    }
    if (timeDiff < weekInMillis) {
        return "Last $dayOfWeek at $timeString"
    }
    if (yearNow == yearNotif) {
        return "$dateSameYear at $timeString"
    }

    return "$dateOtherYear at $timeString"
}