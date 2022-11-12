package com.example.hobbyclubs.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.navigation.NavRoutes

@Composable
fun HomeScreen(navController: NavController) {
    Surface() {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            FakeButtonForNavigationTest(destination = "Clubs") {
                navController.navigate(NavRoutes.ClubsScreen.route)
            }
            FakeButtonForNavigationTest(destination = "News") {
                navController.navigate(NavRoutes.NewsScreen.route)
            }
            FakeButtonForNavigationTest(destination = "Calendar") {
                navController.navigate(NavRoutes.CalendarScreen.route)
            }
            FakeButtonForNavigationTest(destination = "Club Page") {
                navController.navigate(NavRoutes.ClubPageScreen.route)
            }
            FakeButtonForNavigationTest(destination = "Clubs") {
                navController.navigate(NavRoutes.ClubsScreen.route)
            }
            FakeButtonForNavigationTest(destination = "Log out") {
                FirebaseHelper.logout()
                navController.navigate(NavRoutes.LoginScreen.route)
            }
        }
    }
}

@Composable
fun FakeButtonForNavigationTest(destination: String, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier.padding(10.dp)
    ) {
        Text(text = destination)
    }
}