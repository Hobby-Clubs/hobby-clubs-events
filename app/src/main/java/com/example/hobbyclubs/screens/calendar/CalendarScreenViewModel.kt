package com.example.hobbyclubs.screens.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class CalendarScreenViewModel() : ViewModel() {
    private val selectionFlow = MutableStateFlow(emptyList<LocalDate>())

    val eventsFlow = MutableStateFlow(
        listOf(
            Event(LocalDate.now(), "Event Today", false, false),
            Event(LocalDate.now().plusDays(1), "Tournament 1", true, false),
            Event(LocalDate.now().plusDays(3), "Tournament 2", false, false),
            Event(LocalDate.now().plusDays(5), "Tournament 3", true, true),
            Event(LocalDate.now().plusDays(-2), "Tournament 4 - 1", false, true),
            Event(LocalDate.now().plusDays(-2), "Tournament 4 - 2", false, true),
            Event(LocalDate.now().plusDays(-2), "Tournament 4 - 3", false, true),
            Event(LocalDate.now().plusDays(-2), "Tournament 4 - 4", false, true),
            )
    )

    val selectedDayEvents = eventsFlow.combine(selectionFlow) { events, selection ->
        events.filter { it.date in selection }
    }

    fun onSelectionChanged(selection: List<LocalDate>) {
        selectionFlow.value = selection
    }
}

data class Event(
    val date: LocalDate,
    val name: String,
    val liked: Boolean,
    val joined: Boolean,
)