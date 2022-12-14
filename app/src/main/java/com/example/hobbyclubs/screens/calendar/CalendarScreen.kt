package com.example.hobbyclubs.screens.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.*
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import org.joda.time.DateTimeComparator
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

/**
 * Calendar screen for displaying of events within a customizable, selectable calendar
 *
 * @param navController To manage app navigation within the NavHost
 * @param vm [CalendarScreenViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    vm: CalendarScreenViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        vm.onSelectionChanged(listOf(LocalDate.now()))
    }

    val state = rememberSelectableCalendarState(
        confirmSelectionChange = { vm.onSelectionChanged(it); true },
        initialSelectionMode = SelectionMode.Single,
        initialSelection = listOf(LocalDate.now())
    )

    val suggestedIsChecked = remember { mutableStateOf(true) }
    val joinedIsChecked = remember { mutableStateOf(true) }
    val likedIsChecked = remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val dateTimeComparator = DateTimeComparator.getDateOnlyInstance()
    val allEvents by vm.allEvents.observeAsState(listOf())
    val selection by vm.selection.observeAsState()

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
            if (suggestedIsChecked.value && joinedIsChecked.value && likedIsChecked.value) {
                eventsFromSelectedDay
            } else if (suggestedIsChecked.value && joinedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.participants.contains(FirebaseHelper.uid) || !event.likers.contains(
                        FirebaseHelper.uid
                    )
                }
            } else if (suggestedIsChecked.value && likedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    !event.participants.contains(FirebaseHelper.uid) || (event.likers.contains(
                        FirebaseHelper.uid
                    ) && !event.participants.contains(FirebaseHelper.uid))
                }
            } else if (joinedIsChecked.value && likedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.participants.contains(FirebaseHelper.uid) || event.likers.contains(
                        FirebaseHelper.uid
                    )
                }
            } else if (suggestedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    !event.participants.contains(FirebaseHelper.uid) && !event.likers.contains(
                        FirebaseHelper.uid
                    )
                }
            } else if (joinedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.participants.contains(FirebaseHelper.uid)
                }
            } else if (likedIsChecked.value) {
                eventsFromSelectedDay.filter { event ->
                    event.likers.contains(FirebaseHelper.uid) && !event.participants.contains(
                        FirebaseHelper.uid
                    )
                }
            } else {
                listOf()
            }
        }
    }

    val surface3 = if (!isSystemInDarkTheme()) {
        surface3light
    } else {
        surface3dark
    }

    DrawerScreen(
        navController = navController,
        drawerState = drawerState,
        topBar = { MenuTopBar(drawerState = drawerState) }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    SelectableCalendar(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(28.dp)
                            )
                            .background(color = surface3)
                            .padding(horizontal = 12.dp, vertical = 24.dp),
                        firstDayOfWeek = DayOfWeek.MONDAY,
                        calendarState = state,
                        showAdjacentMonths = false,
                        dayContent = { dayState ->
                            Day(
                                state = dayState,
                                event = allEvents.firstOrNull {
                                    (dateTimeComparator.compare(
                                        it.date.toDate(),
                                        dayState.date.toDate()
                                    )) == 0 && !(it.participants.contains(FirebaseHelper.uid) || it.likers.contains(
                                        FirebaseHelper.uid
                                    )) && suggestedIsChecked.value
                                },
                                joinedEvent = allEvents.firstOrNull {
                                    (dateTimeComparator.compare(
                                        it.date.toDate(),
                                        dayState.date.toDate()
                                    )) == 0 && it.participants.contains(FirebaseHelper.uid)
                                            && joinedIsChecked.value
                                },
                                likedEvent = allEvents.firstOrNull {
                                    (dateTimeComparator.compare(
                                        it.date.toDate(),
                                        dayState.date.toDate()
                                    )) == 0 && it.likers.contains(FirebaseHelper.uid) && !it.participants.contains(
                                        FirebaseHelper.uid
                                    )
                                            && likedIsChecked.value
                                },
                            )
                        },
                        monthHeader = { MonthHeader(monthState = state.monthState) },
                        weekHeader = { WeekHeader() }
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = suggestedIsChecked.value,
                                onClick = { suggestedIsChecked.value = !suggestedIsChecked.value },
                                colors = RadioButtonDefaults.colors(
                                    suggestedEventColor
                                )
                            )
                            Text("Suggested", fontSize = 12.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = joinedIsChecked.value,
                                onClick = { joinedIsChecked.value = !joinedIsChecked.value },
                                colors = RadioButtonDefaults.colors(
                                    md_theme_light_primary
                                )
                            )
                            Text("Joined", fontSize = 12.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = likedIsChecked.value,
                                onClick = { likedIsChecked.value = !likedIsChecked.value },
                                colors = RadioButtonDefaults.colors(
                                    md_theme_light_tertiary
                                )
                            )
                            Text("Liked", fontSize = 12.sp)
                        }
                    }
                }

                if (eventsFromSelectedDay.isNotEmpty()) {
                    items(filteredEvents) { event ->
                        EventTile(
                            modifier = Modifier.padding(bottom = 8.dp),
                            event = event,
                            onClick = {
                                navController.navigate(NavRoute.Event.name + "/${event.id}")
                            }, navController = navController
                        )
                    }
                }
            }
        }
    }
}


/**
 * Customizable and selectable day container within the calendar
 *
 * @param state DayState, contains information about current selection as well as date of rendered day
 * @param event Event object
 * @param joinedEvent Status of whether or not the user has joined an event on that day
 * @param likedEvent Status of whether or not the user has liked an event on that day
 * @param modifier Collection of modifier elements that decorate or add behavior to the UI element
 */
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
    val surface3 = if (!isSystemInDarkTheme()) {
        surface3light
    } else {
        surface3dark
    }
    val isColored = joinedEvent != null || likedEvent != null || event != null
    val textColorToday = if (isSelected && !isColored) {
        colorScheme.primary
    } else if (isColored) {
        md_theme_dark_onSurface
    } else {
        colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        border = if (isSelected)
            BorderStroke(2.dp, md_theme_light_primary)
                else null,
        colors = CardDefaults.cardColors(
            contentColor = textColorToday,
            containerColor =
            if (joinedEvent != null)
                md_theme_light_primary
            else if (likedEvent != null)
                md_theme_light_tertiary
            else if (event != null)
                suggestedEventColor
            else
                surface3
        ),
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { selectionState.selection = listOf(date) },
            contentAlignment = Alignment.Center,
        ) {
            if (state.isCurrentDay) {
                Card(
                    modifier = Modifier.size(25.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, textColorToday),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {}
            }
            Text(
                text = date.dayOfMonth.toString(),
                style = if (isColored || isSelected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Customizable month header of the calendar
 *
 * @param monthState MonthState
 * @param modifier Collection of modifier elements that decorate or add behavior to the UI element
 */
@Composable
fun MonthHeader(monthState: MonthState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.minusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowLeft,
                colorFilter = ColorFilter.tint(colorScheme.onSurface),
                contentDescription = "Previous",
            )
        }
        Text(
            text = monthState.currentMonth.month.toString().lowercase()
                .replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = monthState.currentMonth.year.toString(),
            style = MaterialTheme.typography.titleLarge
        )

        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.plusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowRight,
                colorFilter = ColorFilter.tint(colorScheme.onSurface),
                contentDescription = "Next",
            )
        }

    }
}

/**
 * Customizable week header of the calendar
 *
 */
@Composable
fun WeekHeader(
) {
    Row(
        modifier = Modifier.padding(bottom = 14.dp, top = 20.dp)
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayOfWeek ->
            Text(
                text = dayOfWeek,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
            )
        }
    }
}
