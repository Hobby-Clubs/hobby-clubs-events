package com.example.hobbyclubs.screens.clubmanagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
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
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.CustomAlertDialog
import com.example.hobbyclubs.general.SmallTileForClubManagement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubAllNewsScreen(
    navController: NavController,
    vm: ClubManagementViewModel = viewModel(),
    clubId: String
) {
    val club by vm.selectedClub.observeAsState(null)
    val listOfNews by vm.listOfNews.observeAsState(null)

    LaunchedEffect(Unit) {
        vm.getClub(clubId)
        vm.getAllNews(clubId)
    }
    club?.let { club ->
        Scaffold() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = it.calculateBottomPadding(), vertical = 20.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Club News",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                )
                listOfNews?.let {
                    ListOfNews(it, vm)
                }
            }
            CenterAlignedTopAppBar(
                title = { Text(text = club.name, fontSize = 16.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    }
}

@Composable
fun ListOfNews(list: List<News>, vm: ClubManagementViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn {
        items(list) { news ->
            SmallTileForClubManagement(
                data = news,
                onClick = {
                    // TODO navigate to news page
                },
                onDelete = {
                    vm.updateSelection(newsId = news.id)
                    showDeleteDialog = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    if (showDeleteDialog) {
        CustomAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteNews()
                showDeleteDialog = false
            },
            title = "Delete?",
            text = "Are you sure you want to delete? This action cannot be undone.",
            confirmText = "Delete"
        )
    }
}