package com.example.hobbyclubs.screens.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class CalendarScreenViewModel() : ViewModel() {
    private val selectionFlow = MutableStateFlow(emptyList<LocalDate>())

    // MOCK DATA
    val eventsFlow = MutableStateFlow(
        listOf(
            Event(LocalDate.now(), "Ice Hockey Tournament", false, false),
            Event(LocalDate.now().plusDays(1), "Chess Game Night", true, false),
            Event(LocalDate.now().plusDays(3), "Ice Hockey Tournament", false, false),
            Event(LocalDate.now().plusDays(5), "Board Games Club - Game Night", true, true),
            Event(LocalDate.now().plusDays(-2), "Ice Hockey Tournament", false, true),
            Event(LocalDate.now().plusDays(-2), "Table Tennis Tournament", true, false),
            Event(LocalDate.now().plusDays(-2), "Tennis Tournament", true, true),
            Event(LocalDate.now().plusDays(-2), "Chess Tournament", false, false),
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