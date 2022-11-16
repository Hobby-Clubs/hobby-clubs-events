package com.example.hobbyclubs.screens.clubs

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.clubTileBg
import com.example.compose.clubTileBorder
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.DrawerScreen
import com.example.hobbyclubs.general.LazyColumnHeader
import com.example.hobbyclubs.general.MenuTopBar
import com.example.hobbyclubs.navigation.NavRoutes
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.storage.StorageReference

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ClubsScreen(navController: NavController, vm: ClubsScreenViewModel = viewModel()) {
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
                items(suggestedClubs) {
                    ClubTile(
                        club = it,
                        logoRef = vm.getLogo(it.ref),
                        bannerRef = vm.getBanner(it.ref),
                        onClick = {
                            navController.navigate(NavRoutes.ClubPageScreen.route + "/${it.ref}")
                        })
                }
                stickyHeader {
                    LazyColumnHeader(text = "All Clubs")
                }
                items(allClubs) {
                    ClubTile(
                        club = it,
                        logoRef = vm.getLogo(it.ref),
                        bannerRef = vm.getBanner(it.ref),
                        onClick = {
                            navController.navigate(NavRoutes.ClubPageScreen.route + "/${it.ref}")
                        })
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

    }
//    AddMockClubs(vm = vm)
}

//@Composable
//fun ClubList(clubs: List<Club>, vm: ClubsScreenViewModel) {
//    Column(modifier = Modifier.fillMaxWidth()) {
//        LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
//            items(clubs) {
//                ClubTile(club = it, vm = vm)
//            }
//        }
//    }
//}

@Composable
fun ClubTile(
    modifier: Modifier = Modifier,
    club: Club,
    logoRef: StorageReference,
    bannerRef: StorageReference,
    onClick: () -> Unit
) {
    var logoUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var bannerUri: Uri? by rememberSaveable { mutableStateOf(null) }
    val isJoined = club.members.contains(FirebaseHelper.uid)

    LaunchedEffect(Unit) {
        if (logoUri == null) {
            logoRef
                .downloadUrl
                .addOnSuccessListener {
                    logoUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogo", "ClubTile: ", it)
                }
        }
        if (bannerUri == null) {
            bannerRef
                .downloadUrl
                .addOnSuccessListener {
                    bannerUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogo", "ClubTile: ", it)
                }
        }
    }

    if (logoUri != null && logoUri != null) {
        Card(
            modifier = modifier.clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, clubTileBorder),
            colors = CardDefaults.cardColors(clubTileBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4.3f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(50.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(logoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "logo",
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = club.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text(text = if (isJoined) "Already joined" else "Join now!", fontSize = 14.sp)
                    }
                }
                AsyncImage(
                    modifier = Modifier.weight(1f),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(bannerUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddMockClubs(vm: ClubsScreenViewModel) {
//    var logoUri: Uri? by remember { mutableStateOf(null) }
//    var bannerUri: Uri? by remember { mutableStateOf(null) }
//    var amount: Int? by remember { mutableStateOf(null) }
//
//    val logoLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = {
//            logoUri = it
//        })
//
//    val bannerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = {
//            bannerUri = it
//        })
//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Button(onClick = { logoLauncher.launch("image/*") }) {
//                if (logoUri == null) {
//                    Text(text = "Add Logo")
//                } else {
//                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "logo")
//                }
//            }
//            Button(onClick = { bannerLauncher.launch("image/*") }) {
//                if (bannerUri == null) {
//                    Text(text = "Add banner")
//                } else {
//                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "banner")
//                }
//            }
//            OutlinedTextField(
//                value = amount?.toString() ?: "",
//                onValueChange = { amount = it.toIntOrNull() },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                textStyle = TextStyle(textAlign = TextAlign.Center)
//            )
//            Button(onClick = {
//                if (logoUri == null || bannerUri == null || amount == null) {
//                    println("Missing Uri")
//                    return@Button
//                }
//                val mockClub = Club()
//                vm.addMockClubs(amount!!, mockClub, logoUri!!, bannerUri!!)
//            }) {
//                Text(text = "Add")
//            }
//        }
//    }
//}