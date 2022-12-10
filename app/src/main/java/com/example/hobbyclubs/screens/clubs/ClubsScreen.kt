package com.example.hobbyclubs.screens.clubs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.general.DrawerScreen
import com.example.hobbyclubs.general.LazyColumnHeader
import com.example.hobbyclubs.general.MenuTopBar
import com.example.hobbyclubs.navigation.NavRoute
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * Clubs screen displays suggestions for new clubs for the user. Under the suggestions
 * you can see all the clubs.
 *
 * @param navController for Compose navigation
 * @param vm [ClubsScreenViewModel]
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(
    navController: NavController,
    vm: ClubsScreenViewModel = viewModel(),
) {
    val suggestedClubs by vm.suggestedClubs.observeAsState(listOf())
    val allClubs by vm.clubs.observeAsState(listOf())
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isRefreshing by vm.isRefreshing.observeAsState(false)

    DrawerScreen(
        navController = navController,
        drawerState = drawerState,
        topBar = { MenuTopBar(drawerState = drawerState) }) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = { vm.refresh() },
            refreshTriggerDistance = 50.dp
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stickyHeader {
                    LazyColumnHeader(text = "Suggested Clubs")
                }
                items(suggestedClubs) { club ->
                    ClubTile(
                        club = club,
                    ) {
                        navController.navigate(NavRoute.ClubPage.name + "/${club.ref}")
                    }
                }
                stickyHeader {
                    LazyColumnHeader(text = "All Clubs")
                }
                items(allClubs) { club ->
                    ClubTile(
                        club = club,
                    ) {
                        navController.navigate(NavRoute.ClubPage.name + "/${club.ref}")
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}