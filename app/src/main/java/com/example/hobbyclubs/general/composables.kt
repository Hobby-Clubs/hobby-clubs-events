package com.example.hobbyclubs.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BasicText(value: String) {
    Text(text = value)
}

@Composable
fun DividerLine(width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(1.dp)
            .background(color = Color.Black)
    )
}