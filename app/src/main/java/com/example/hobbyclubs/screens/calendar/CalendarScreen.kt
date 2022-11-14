package com.example.hobbyclubs.screens.calendar

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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import com.example.hobbyclubs.general.DrawerScreen
import com.example.hobbyclubs.general.MenuTopBar
import com.example.hobbyclubs.screens.clubpage.EventTile
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import io.github.boguszpawlowski.composecalendar.selection.SelectionState
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController, vm: CalendarScreenViewModel = viewModel()) {
    val state = rememberSelectableCalendarState(
        initialSelection = listOf(LocalDate.now()),
        confirmSelectionChange = { vm.onSelectionChanged(it); true },
        initialSelectionMode = SelectionMode.Single,
    )

    val events by vm.eventsFlow.collectAsState()
    val selectedDayEvents by vm.selectedDayEvents.collectAsState(null)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    DrawerScreen(
        navController = navController,
        drawerState = drawerState,
        topBar = {  }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.fillMaxHeight()) {
                SelectableCalendar(
                    modifier = Modifier.padding(5.dp),
                    firstDayOfWeek = DayOfWeek.MONDAY,
                    calendarState = state,
                    showAdjacentMonths = false,
                    dayContent = { dayState ->
                        val likedEvents = events.filter { it.liked }
                        val joinedEvents = events.filter { it.joined }
                        DefaultDay(
                            state = dayState,
                            event = events.firstOrNull { it.date == dayState.date },
                            likedEvent = likedEvents.firstOrNull { it.date == dayState.date },
                            joinedEvent = joinedEvents.firstOrNull { it.date == dayState.date }
                        )
                    },
                    monthHeader = {
                        MonthHeader(monthState = state.monthState)
                    }
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
                if(selectedDayEvents?.isEmpty() == false) {
                    LazyColumn(modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp, vertical = 5.dp), contentPadding = PaddingValues(5.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selectedDayEvents!!) { event ->
                            Event(event.name, event.date)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(monthState: MonthState, modifier: Modifier = Modifier, ) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.minusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowLeft,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = "Previous",
            )
        }
        Text(
            text = monthState.currentMonth.month.toString(),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = monthState.currentMonth.year.toString(), style = MaterialTheme.typography.headlineSmall)
        IconButton(
            onClick = { monthState.currentMonth = monthState.currentMonth.plusMonths(1) }
        ) {
            Image(
                imageVector = Icons.Default.KeyboardArrowRight,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                contentDescription = "Next",
            )
        }
    }
}

@Composable
fun <T : SelectionState> DefaultDay(
    state: DayState<T>,
    modifier: Modifier = Modifier,
    event: Event?,
    likedEvent: Event?,
    joinedEvent: Event?,
    onClick: (LocalDate) -> Unit = {},
) {
    val date = state.date
    val selectionState = state.selectionState

    val isSelected = selectionState.isDateSelected(date)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = (if (state.isFromCurrentMonth) 4.dp else 1.dp)),
        border = if(joinedEvent != null) BorderStroke(1.dp, joinedColor) else if(likedEvent != null) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else if(event != null) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            contentColor = (if (state.isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
            containerColor = if(isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick(date)
                    selectionState.onDateSelected(date)
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(text = date.dayOfMonth.toString())
        }
    }
}

@Composable
fun Event(title: String, date: LocalDate) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hockey),
                    contentDescription = "Tile background",
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = "Ice Hockey Tournament",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 18.sp,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset.Zero,
                            blurRadius = 5f
                        )
                    )
                )
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        "Favourite icon",
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                RowItem(icon = Icons.Outlined.CalendarMonth, iconDesc = "Calendar Icon", content = date.toString() )
                RowItem(icon = Icons.Outlined.Timer, iconDesc = "Timer Icon", content = "19:00")
                RowItem(icon = Icons.Outlined.People, iconDesc = "People Icon", content = "5")
            }
        }

    }
}

@Composable
fun RowItem(icon: ImageVector, iconDesc: String, content: String) {
    Row() {
        Icon(icon, iconDesc)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = content)
    }
}