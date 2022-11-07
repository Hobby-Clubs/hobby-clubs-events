package com.example.hobbyclubs.screens.calendarscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.boguszpawlowski.composecalendar.StaticCalendar

@Composable
fun CalendarScreen(
    vm: CalendarScreenViewModel = viewModel()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StaticCalendar()
    }
}