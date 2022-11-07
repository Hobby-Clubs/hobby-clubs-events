package com.example.hobbyclubs.screens.clubpage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbyclubs.R
import com.example.hobbyclubs.general.DividerLine
import com.example.hobbyclubs.navigation.NavRoutes

@Composable
fun ClubPageScreen(navController: NavController) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ClubPageHeader(navController)
        DividerLine(width = (screenWidth * 0.9).dp)
    }
}

@Composable
fun ClubPageHeader(navController: NavController) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val screenWidth = LocalConfiguration.current.screenWidthDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.hockey),
                contentDescription = "background image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.25).dp),
                contentScale = ContentScale.FillWidth
            )
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
                    .width((screenWidth / 2 ).dp)
                    .border(BorderStroke(1.dp, Color.Black))
            ) {
                Text(
                    text = "Ice Hockey Club",
                    fontSize = 20.sp
                )
                TextButton(onClick = { navController.navigate(NavRoutes.ClubManagementScreen.route) }) {
                    Row() {
                        Text(text = "20 members")
                        Icon(Icons.Filled.NavigateNext, contentDescription = "arrow right")
                    }
                }
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            ClubLogo(modifier = Modifier)
            Spacer(modifier = Modifier.width(30.dp))
        }
    }
}

@Composable
fun ClubLogo(modifier: Modifier) {
    Card(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.hockey_logo_with_sticks),
            contentDescription = "avatar",
            modifier = Modifier
                .padding(10.dp)
                .size(125.dp)
        )
    }
}