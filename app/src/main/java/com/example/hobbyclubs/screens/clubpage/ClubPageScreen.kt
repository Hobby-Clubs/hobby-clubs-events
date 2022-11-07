package com.example.hobbyclubs.screens.clubpage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ClubPageScreen(navController: NavController) {
    ClubPageHeader()
}

@Composable
fun ClubPageHeader() {
    val screenHeight = LocalConfiguration.current.screenHeightDp * 0.3

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(screenHeight.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {

        }
    }

}