package com.example.hobbyclubs.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.hobbyclubs.navigation.NavRoutes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Surface() {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val items = listOf("Home", "Restaurant", "HobbyClubs", "Parking", "Settings", "Profile")
        val selectedItem = remember { mutableStateOf(items[2]) }
        ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                items.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = item == selectedItem.value,
                        onClick = {
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }, content = {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                Button(onClick = { scope.launch { drawerState.open() } }) {
                    Text("Click to open")
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
            }
        })
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