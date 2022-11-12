package com.example.hobbyclubs.screens.clubs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import com.example.compose.*
import com.example.hobbyclubs.api.Club

@Composable
fun ClubsScreen(navController: NavController, vm: ClubsScreenViewModel = viewModel()) {
    val suggestedClubs by vm.suggestedClubs.observeAsState(listOf())
    val otherClubs by vm.otherClubs.observeAsState(listOf())
    Column(
        Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ClubList(clubs = suggestedClubs, title = "Suggested Clubs", vm = vm)
        Spacer(modifier = Modifier.height(32.dp))
        ClubList(clubs = otherClubs, title = "All Clubs", vm = vm)
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
fun ClubList(clubs: List<Club>, title: String, vm: ClubsScreenViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontWeight = FontWeight.Light, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(24.dp))
        LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(clubs) {
                ClubTile(club = it, vm = vm)
            }
        }
    }
}

@Composable
fun ClubTile(modifier: Modifier = Modifier, club: Club, vm: ClubsScreenViewModel) {
    var logoBitmap: Bitmap? by remember { mutableStateOf(null) }
    var bannerBitmap: Bitmap? by remember { mutableStateOf(null) }
    val byte = 1024L * 1024

    LaunchedEffect(Unit) {
        vm.getLogo(club.ref)
            .getBytes(byte)
            .addOnSuccessListener {
                logoBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            }
            .addOnFailureListener {
                Log.e("getLogo", "ClubTile: ", it)
            }
        vm.getBanner(club.ref)
            .getBytes(byte)
            .addOnSuccessListener {
                bannerBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            }
            .addOnFailureListener {
                Log.e("getLogo", "ClubTile: ", it)
            }
    }

    if (logoBitmap != null && bannerBitmap != null) {
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
                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .padding(0.dp)
                            .padding(end = 8.dp),
                        bitmap = logoBitmap!!.asImageBitmap(),
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
                Image(
                    modifier = Modifier.weight(1f),
                    bitmap = bannerBitmap!!.asImageBitmap(),
                    contentDescription = "banner",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

}