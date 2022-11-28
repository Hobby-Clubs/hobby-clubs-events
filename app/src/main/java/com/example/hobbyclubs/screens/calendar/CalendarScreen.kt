package com.example.hobbyclubs.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.joinedColor
import com.example.compose.liked
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.*
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
fun CalendarScreen(
    navController: NavController,
    vm: CalendarScreenViewModel = viewModel(),
    imageVm: ImageViewModel = viewModel()
) {
    val state = rememberSelectableCalendarState(
        confirmSelectionChange = { vm.onSelectionChanged(it); true },
        initialSelectionMode = SelectionMode.Single,
    )

    val allIsChecked = remember { mutableStateOf(true) }
    val joinedIsChecked = remember { mutableStateOf(true) }
    val likedIsChecked = remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val dateTimeComparator = DateTimeComparator.getDateOnlyInstance()
    val allEvents by vm.allEvents.observeAsState(listOf())
    val selection by vm.selection.observeAsState()

    val listOfUri by imageVm.eventBannerUris.observeAsState(listOf())
    val eventsFromSelectedDay by remember {
        derivedStateOf {
            selection?.let {
                allEvents.filter { event ->
                    (dateTimeComparator.compare(event.date.toDate(), it.first().toDate())) == 0
                }
            } ?: listOf()
        }
    }

    val filteredEvents by remember {
        derivedStateOf {
            if(allIsChecked.value) {
                eventsFromSelectedDay
            } else if(joinedIsChecked.value && likedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.participants.contains(FirebaseHelper.uid) || event.likers.contains(FirebaseHelper.uid)
                }
            }  else if(joinedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.participants.contains(FirebaseHelper.uid)
                }
            } else if(likedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.likers.contains(FirebaseHelper.uid)
                }
            } else {
                listOf()
            }
        }
    }



    LaunchedEffect(allEvents) {
        if (allEvents.isNotEmpty()) {
            imageVm.getEventUris(allEvents)
        }
    }

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
                                (dateTimeComparator.compare(
                                    it.date.toDate(),
                                    dayState.date.toDate()
                                )) == 0 && (allIsChecked.value)
                            },
                            joinedEvent = allEvents.firstOrNull {
                                (dateTimeComparator.compare(
                                    it.date.toDate(),
                                    dayState.date.toDate()
                                )) == 0 && it.participants.contains(FirebaseHelper.uid)
                                        && (allIsChecked.value || joinedIsChecked.value)
                            },
                            likedEvent = allEvents.firstOrNull {
                                (dateTimeComparator.compare(
                                    it.date.toDate(),
                                    dayState.date.toDate()
                                )) == 0 && it.likers.contains(FirebaseHelper.uid)
                                        && (allIsChecked.value || likedIsChecked.value)
                            },
                        )
                    },
                    monthHeader = { MonthHeader(monthState = state.monthState) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /*
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    */
                    Checkbox(
                        checked = allIsChecked.value,
                        onCheckedChange = {
                            allIsChecked.value = it

                        },
                        colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.primary),

                    )
                    Text("All", fontSize = 12.sp)
                    /*
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = joinedColor,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    */
                    Checkbox(
                        checked = joinedIsChecked.value,
                        onCheckedChange = {
                            joinedIsChecked.value = it

                        },
                        colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.secondary),
                        enabled = (!allIsChecked.value)

                    )
                    Text("Joined", fontSize = 12.sp)
                    /*
                    Icon(
                        imageVector = Icons.Filled.Crop169,
                        contentDescription = "color",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .width(30.dp)
                            .height(30.dp)
                    )
                    */
                    Checkbox(
                        checked = likedIsChecked.value,
                        onCheckedChange = { likedIsChecked.value = it },
                        colors = CheckboxDefaults.colors(MaterialTheme.colorScheme.tertiary),
                        enabled = (!allIsChecked.value)
                    )
                    Text("Liked", fontSize = 12.sp)
                }

                if (eventsFromSelectedDay.isNotEmpty() && listOfUri.isNotEmpty()) {
                    LazyColumn(modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 5.dp),
                        contentPadding = PaddingValues(5.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredEvents) { event ->
                            val uri = listOfUri!!.find { it.first == event.id }?.second
                            EventTile(
                                event = event,
                                picUri = uri,
                                onClick = {
                                    navController.navigate(NavRoutes.EventScreen.route + "/${event.id}")
                                },
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
    joinedEvent: Event?,
    likedEvent: Event?,
    modifier: Modifier = Modifier,
) {
    val date = state.date
    val selectionState = state.selectionState
    val isSelected = selectionState.isDateSelected(date)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (if (state.isFromCurrentMonth) 4.dp else 1.dp)),
        border =
        if (joinedEvent != null)
            BorderStroke(1.dp, joinedColor)
        else if (likedEvent != null)
            BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        else if (event != null)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else null,
        colors = CardDefaults.cardColors(
            contentColor = (if (state.isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { selectionState.onDateSelected(date) },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = date.dayOfMonth.toString())
        }
    }
}

@Composable
fun MonthHeader(monthState: MonthState, modifier: Modifier = Modifier) {
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
        Text(
            text = monthState.currentMonth.year.toString(),
            style = MaterialTheme.typography.headlineSmall
        )
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
