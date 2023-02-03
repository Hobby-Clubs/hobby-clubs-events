package com.example.hobbyclubs.screens.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.md_theme_light_primary
import com.example.compose.*
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.general.toString
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.notifications.NotificationContent
import com.example.hobbyclubs.screens.settings.NotificationSetting
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
/**
 * Screen which shows all the unread in app notifications of a the current user, according
 * to their notification settings.
 * The user can then mark them as read individually or all at once
 *
 * @param navController
 * @param vm
 */
@Composable
fun NotificationsScreen(
    navController: NavController,
    vm: NotificationScreenViewModel = viewModel()
) {
    val context = LocalContext.current
    val unreads by vm.unreads.observeAsState()
    val isRefreshing by vm.isRefreshing.observeAsState(false)

    // starts the broadcast receiver for in app notifications then stops it when the screen is closed
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
                    ClearAllButton {
                        vm.markAllAsRead(it)
                    }
                } else {
                    FilledTonalButton(onClick = { vm.removeRead() }) {
                        Icon(
                            Icons.Outlined.Restore, "restore",
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(text = "Reset")
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
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
                            onMarkAsRead = { vm.markAsRead(it, data) },
                            onClick = { navController.navigate(it) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Shows a text when there are no new in app notifications
 *
 */
@Composable
fun NoNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "No new notifications")
    }
}

/**
 * Shows a loading animation and text when the app is fetching the in app notifications
 *
 */
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

/**
 * List of all unread in app notifications, according to the notification settings.
 * Can be pulled down to fetch the in app notifications again
 *
 * @param isRefreshing
 * @param onRefresh
 * @param contents
 * @param onMarkAsRead
 * @param onClick
 */
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

/**
 * Top app bar for the NotificationScreen
 *
 * @param navController
 * @param onClickSettings
 * @receiver
 */
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

/**
 * Tile that represents an unread in app notification with its icon, title and description.
 * Can be dismissed by swiping left and opens a relevant screen when pressed
 *
 * @param content
 * @param onDismiss
 * @param onClick
 */
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
                    else -> colorScheme.error
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
                    )
                }
            }
        }
    ) {
        ElevatedCard(modifier = Modifier
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
                        NotificationSetting.EVENT_NEW -> colorScheme.primary
                        NotificationSetting.EVENT_HOUR_REMINDER -> colorScheme.error
                        NotificationSetting.EVENT_DAY_REMINDER -> colorScheme.error
                        NotificationSetting.NEWS_GENERAL -> colorScheme.surfaceVariant
                        NotificationSetting.NEWS_CLUB -> colorScheme.primary
                        NotificationSetting.REQUEST_MEMBERSHIP -> colorScheme.primary
                        NotificationSetting.REQUEST_MEMBERSHIP_ACCEPTED -> colorScheme.primary
                        NotificationSetting.REQUEST_PARTICIPATION -> colorScheme.primary
                        NotificationSetting.REQUEST_PARTICIPATION_ACCEPTED -> colorScheme.primary
                    }
                    Card(
                        modifier = Modifier.padding(end = 8.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(color),
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .padding(10.dp)
                        )
                    }
                }
                Column {
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

/**
 * Button to mark all the current in app notifications as read
 *
 * @param modifier
 * @param onClick
 * @receiver
 */
@Composable
fun ClearAllButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(100.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primary),
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
                tint = colorScheme.onPrimary,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(18.dp)
            )
            Text(
                text = "Clear all",
                color = colorScheme.onPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Returns the date of an in app notification formatted in a relevant manner
 *
 * @param notificationDate
 * @return a string of the date of a notification
 */
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