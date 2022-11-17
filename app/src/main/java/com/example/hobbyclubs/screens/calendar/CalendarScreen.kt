package com.example.hobbyclubs.screens.calendar

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop169
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.R
import com.example.compose.joinedColor
import com.example.compose.nokiaBlue
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.DrawerScreen
import com.example.hobbyclubs.general.EventTile
import com.example.hobbyclubs.general.MenuTopBar
import com.example.hobbyclubs.general.toDate
import com.example.hobbyclubs.navigation.NavRoutes
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import org.joda.time.DateTimeComparator
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, vm: CalendarScreenViewModel = viewModel()) {
    val state = rememberSelectableCalendarState(
        confirmSelectionChange = { vm.onSelectionChanged(it); true },
        initialSelectionMode = SelectionMode.Single,
    )
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val allEvents by vm.allEvents.observeAsState(listOf())
    val filteredEvents by vm.filteredEvents.observeAsState(listOf())
    val dateTimeComparator = DateTimeComparator.getDateOnlyInstance()

    DrawerScreen(
        navController = navController,
        drawerState = drawerState,
        topBar = { MenuTopBar(drawerState = drawerState) }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxHeight()) {
                SelectableCalendar(
                    modifier = Modifier.padding(5.dp),
                    firstDayOfWeek = DayOfWeek.MONDAY,
                    calendarState = state,
                    showAdjacentMonths = true,
                    dayContent = { dayState ->
                        Day(
                            state = dayState,
                            event = allEvents.firstOrNull {
                                (dateTimeComparator.compare(it.date.toDate(), dayState.date.toDate())) == 0
                            }
                        )
                    },
                    monthHeader = { MonthHeader(monthState = state.monthState) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    Text("Suggested", fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = joinedColor,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    Text("Joined", fontSize = 12.sp)
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    Text("Liked", fontSize = 12.sp)
                }

                if(!filteredEvents.isEmpty()) {
                    LazyColumn(modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 5.dp),
                        contentPadding = PaddingValues(5.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredEvents) {
                            EventTile(
                                event = it,
                                onClick = {
                                    navController.navigate(NavRoutes.EventScreen.route + "/${it.id}")
                                },
                                onJoin = {
                                    vm.joinEvent(it)
                                },
                                onLike = {
                                    if (it.likers.contains(FirebaseHelper.uid)) {
                                        vm.removeLikeOnEvent(it)
                                    } else {
                                        vm.likeEvent(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Day(
    state: DayState<DynamicSelectionState>,
    event: Event?,
    modifier: Modifier = Modifier,
) {
    val date = state.date
    val selectionState = state.selectionState
    val isSelected = selectionState.isDateSelected(date)

    // temporary
    val joinedStatus = false
    val likedStatus = false

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (if (state.isFromCurrentMonth) 4.dp else 1.dp)),
        border =
        if(joinedStatus)
            BorderStroke(1.dp, joinedColor)
        else if(likedStatus)
            BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        else if(event != null)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        colors = CardDefaults.cardColors(
            contentColor = (if (state.isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    Log.d("ONSELECT", event.toString())
                    selectionState.onDateSelected(date)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = date.dayOfMonth.toString())
        }
    }
}

@Composable
fun MonthHeader(monthState: MonthState, modifier: Modifier = Modifier, ) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        /*
        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.minusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowLeft,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = "Previous",
            )
        }
        */
        Text(
            text = monthState.currentMonth.month.toString(),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = monthState.currentMonth.year.toString(), style = MaterialTheme.typography.headlineSmall)
        /*
        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.plusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowRight,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = "Next",
            )
        }
        */
    }
}
