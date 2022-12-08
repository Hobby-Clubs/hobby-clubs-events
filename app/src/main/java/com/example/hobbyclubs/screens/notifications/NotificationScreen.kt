package com.example.hobbyclubs.screens.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FabPosition
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Settings
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
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.general.toString
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.notifications.NotificationContent
import com.example.hobbyclubs.screens.settings.NotificationSetting
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@Composable
fun NotificationsScreen(
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
                onClickSettings = { navController.navigate(NavRoute.NotificationSettings.route) })
        },
        floatingActionButton = {
            unreads?.let {
                if (it.isNotEmpty()) {
                    ClearAllButton() {
                        vm.markAllAsRead(it)
                    }
                } else {
                    FloatingActionButton(onClick = { vm.removeRead() }) {
                       Text(text = "Reset")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { pad ->
        Box(modifier = Modifier.padding(pad)) {
            if (unreads == null) {
                NotificationsLoading()
            } else {
                unreads?.let { data ->
                    if (data.isEmpty()) {
                        NoNotifications()
                    } else {
                        NotificationList(
                            isRefreshing = isRefreshing,
                            onRefresh = { vm.refresh() },
                            contents = data,
                            onMarkAsRead = { vm.markAsRead(it) },
                            onClick = { navController.navigate(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No new notifications")
    }
}

@Composable
fun NotificationsLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(text = "Loading notifications")
        }
    }
}

@Composable
fun NotificationList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    contents: List<NotificationContent>,
    onMarkAsRead: (String) -> Unit,
    onClick: (String) -> Unit
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = { onRefresh() },
        refreshTriggerDistance = 50.dp
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp, start = 8.dp, end = 8.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(contents, { item -> item.id }) {
                NotificationTile(
                    content = it,
                    onDismiss = { id ->
                        onMarkAsRead(id)
                    },
                    onClick = {
                        it.navRoute?.let { route ->
                            onClick(route)
                        }
                    })
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
                        NotificationSetting.REQUEST_PARTICIPATION -> md_theme_light_primary
                        NotificationSetting.REQUEST_MEMBERSHIP_ACCEPTED -> Color.Green
                        NotificationSetting.REQUEST_MEMBERSHIP -> md_theme_light_primary
                        NotificationSetting.REQUEST_PARTICIPATION_ACCEPTED -> Color.Green
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

@Composable
fun ClearAllButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
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
                Icons.Outlined.ClearAll,
                "clear icon",
                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(18.dp)
            )
            Text(
                text = "Clear all",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
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
        return "$dayOfWeek at $timeString"
    }
    if (yearNow == yearNotif) {
        return "$dateSameYear at $timeString"
    }

    return "$dateOtherYear at $timeString"
}