package com.example.hobbyclubs.screens.news

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.CollectionName.Companion.news

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleNewsScreen(
    navController: NavController,
    vm: SingleScreenViewModel = viewModel(),
    newsId: String
    ) {


    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.getNews(newsId)
        vm.getImage(newsId)
    }
    news?.let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black
                            )
                        }
                    }
                )
            }
        }

    }
}