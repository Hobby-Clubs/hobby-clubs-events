package com.example.hobbyclubs.screens.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.day.DayState
import io.github.boguszpawlowski.composecalendar.header.MonthState
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode

@Composable
fun CalendarScreen(navController: NavController, vm: CalendarScreenViewModel = viewModel()) {
    val state = rememberSelectableCalendarState(
        confirmSelectionChange = { vm.onSelectionChanged(it); true },
        initialSelectionMode = SelectionMode.Single,
    )

    val events by vm.eventsFlow.collectAsState()
    val selectedDayEvents by vm.selectedDayEvents.collectAsState(null)

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column() {
            SelectableCalendar(
                calendarState = state,
                dayContent = { dayState ->
                    EventDay(
                        state = dayState,
                        event = events.firstOrNull { it.date == dayState.date },
                    )
                },
                monthHeader = {
                    MonthHeader(monthState = state.monthState)
                }
            )

            if(selectedDayEvents != null) {
                LazyColumn {
                    items(selectedDayEvents!!) { event ->
                        Text(event.name)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(
    monthState: MonthState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
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
fun EventDay(
    state: DayState<DynamicSelectionState>,
    event: Event?,
    modifier: Modifier = Modifier,
) {
    val date = state.date
    val selectionState = state.selectionState
    val isSelected = selectionState.isDateSelected(date)

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable {
                selectionState.onDateSelected(date)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = (if (state.isFromCurrentMonth) 4.dp else 1.dp)),
        border = if (state.isCurrentDay) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else if(event != null) BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary) else null,
        colors = CardDefaults.cardColors(
            contentColor = (if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(text = date.dayOfMonth.toString())
        }
    }
}
