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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubNewsScreen(
    navController: NavController,
    vm: ClubPageViewModel = viewModel(),
    clubId: String,
    fromHomeScreen: Boolean
) {
    val listOfNews by vm.listOfNews.observeAsState(null)

    LaunchedEffect(Unit){
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
                    } else{
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