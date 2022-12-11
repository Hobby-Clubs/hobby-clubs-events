package com.example.hobbyclubs.screens.clubpage

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.general.TopBarBackButton


/**
 * Club news screen is used to display all the news of a selected club, it can show either
 * all the club news or only the ones that the user has not read, depending on the
 * value of fromHomeScreen
 *
 * @param navController for Compose navigation
 * @param vm [ClubPageViewModel]
 * @param clubId UID for the club you have selected earlier on home or club screen
 * @param fromHomeScreen If user pressed the button on the home screens club tiles that shows
 * unread news this will be true, if user pressed from the club page this will be false
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubNewsScreen(
    navController: NavController,
    vm: ClubPageViewModel = viewModel(),
    clubId: String,
    fromHomeScreen: Boolean
) {
    val listOfNews by vm.listOfNews.observeAsState(null)

    LaunchedEffect(Unit) {
        vm.getAllNews(clubId, fromHomeScreen)
    }
    listOfNews?.let {
        Log.d("listofnews", "ClubNewsScreen: $it ")
        Scaffold() { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = padding.calculateBottomPadding(), horizontal = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "My club news",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                if (it.isNotEmpty()) {
                    ClubNewsList(it, navController = navController)
                } else {
                    Text(text = "You have 0 unread news")
                }
            }
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    TopBarBackButton(navController = navController)
                }
            )
        }
    }
}