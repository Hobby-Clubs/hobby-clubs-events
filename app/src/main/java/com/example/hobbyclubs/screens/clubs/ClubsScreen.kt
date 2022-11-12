package com.example.hobbyclubs.screens.clubs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.compose.*
import com.example.hobbyclubs.api.Club

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClubsScreen(navController: NavController, vm: ClubsScreenViewModel = viewModel()) {
    val suggestedClubs by vm.suggestedClubs.observeAsState(listOf())
    val otherClubs by vm.clubs.observeAsState(listOf())
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = "Suggested Clubs",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                )
            }
        }
        items(suggestedClubs) {
            ClubTile(club = it, vm = vm)
        }
        stickyHeader {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)) {
                Text(
                    modifier = Modifier.padding(vertical = 24.dp),
                    text = "All Clubs",
                    fontWeight = FontWeight.Light,
                    fontSize = 24.sp
                )
            }
        }
        items(otherClubs) {
            ClubTile(club = it, vm = vm)
        }
    }
//    AddMockClubs(vm = vm)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMockClubs(vm: ClubsScreenViewModel) {
    var logoUri: Uri? by remember { mutableStateOf(null) }
    var bannerUri: Uri? by remember { mutableStateOf(null) }
    var amount: Int? by remember { mutableStateOf(null) }

    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            logoUri = it
        })

    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            bannerUri = it
        })
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { logoLauncher.launch("image/*") }) {
                if (logoUri == null) {
                    Text(text = "Add Logo")
                } else {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "logo")
                }
            }
            Button(onClick = { bannerLauncher.launch("image/*") }) {
                if (bannerUri == null) {
                    Text(text = "Add banner")
                } else {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = "banner")
                }
            }
            OutlinedTextField(
                value = amount?.toString() ?: "",
                onValueChange = { amount = it.toIntOrNull() },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(textAlign = TextAlign.Center)
            )
            Button(onClick = {
                if (logoUri == null || bannerUri == null || amount == null) {
                    println("Missing Uri")
                    return@Button
                }
                val mockClub = Club()
                vm.addMockClubs(amount!!, mockClub, logoUri!!, bannerUri!!)
            }) {
                Text(text = "Add")
            }
        }
    }
}

@Composable
fun ClubList(clubs: List<Club>, vm: ClubsScreenViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(clubs) {
                ClubTile(club = it, vm = vm)
            }
        }
    }
}

@Composable
fun ClubTile(modifier: Modifier = Modifier, club: Club, vm: ClubsScreenViewModel) {
    var logoUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var bannerUri: Uri? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (logoUri == null) {
            vm.getLogo(club.ref)
                .downloadUrl
                .addOnSuccessListener {
                    logoUri = it
                }
                .addOnFailureListener {
                    Log.e("getLogo", "ClubTile: ", it)
                }
        }
        if (bannerUri == null) {
            vm.getBanner(club.ref)
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
            modifier = modifier,
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .size(50.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .padding(0.dp)
                            .padding(end = 8.dp),
                        model = logoUri,
                        contentDescription = "logo"
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = club.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                        Text(text = "Join now!", fontSize = 14.sp)
                    }
                }
                AsyncImage(
                    modifier = Modifier.weight(1f),
                    model = bannerUri,
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

}