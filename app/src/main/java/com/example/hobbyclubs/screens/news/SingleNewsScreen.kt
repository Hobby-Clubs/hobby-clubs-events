package com.example.hobbyclubs.screens.news

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.CustomOutlinedTextField
import com.example.hobbyclubs.general.TopBarBackButton
import com.example.hobbyclubs.general.CustomButton
import com.example.hobbyclubs.general.SelectedImageItem
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun SingleNewsScreen(
    navController: NavController,
    vm: SingleScreenViewModel = viewModel(),
    newsId: String
) {
    val news by vm.selectedNews.observeAsState(null)
    val isPublisher by vm.isPublisher.observeAsState()
    val hasRead by vm.hasRead.observeAsState(null)

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val scope = rememberCoroutineScope()

    BackHandler(sheetState.isVisible) {
        scope.launch { sheetState.hide() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            EditNewsSheet(
                vm = vm,
                newsId = newsId,
                onSave = { scope.launch { if (sheetState.isVisible) sheetState.hide() } }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LaunchedEffect(Unit) {
            vm.getNews(newsId)
            vm.getCurrentUser()
        }
        hasRead?.let {
        LaunchedEffect(it) {
            Log.d("hasread", "SingleNewsScreen: $it ")
            if (!it) {
                val changeMap = mapOf(
                    Pair("usersRead", FieldValue.arrayUnion(FirebaseHelper.uid))
                )
                vm.updateNews(newsId, changeMap)
            }
        }
        }
        LaunchedEffect(news) {
            news?.let {
                vm.fillPreviousClubData(it)
            }
        }
        news?.let {
            Box(Modifier.fillMaxSize()) {
                NewsContent(vm, it)
                CenterAlignedTopAppBar(
                    title = { },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                    actions = {
                        if (isPublisher == true) {
                            Card(
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .size(30.dp)
                                    .aspectRatio(1f)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                sheetState.animateTo(ModalBottomSheetValue.Expanded)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                    },
                    navigationIcon = {
                        TopBarBackButton(navController = navController)
                    }
                )
            }
        }
    }
}

@Composable
fun EditNewsSheet(vm: SingleScreenViewModel, newsId: String, onSave: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val headline by vm.headline.observeAsState(TextFieldValue(""))
    val newsContent by vm.newsContent.observeAsState(TextFieldValue(""))
    val selectedImage by vm.selectedImage.observeAsState(null)
    val loading by vm.loading.observeAsState(false)
    val scope = rememberCoroutineScope()

    val newsImageGallery =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.temporarilyStoreImage(newsUri = uri)
        }

    if (!loading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = (screenHeight * 0.95).dp, max = (screenHeight * 0.95).dp)
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Edit News",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                CustomOutlinedTextField(
                    value = headline ?: TextFieldValue(""),
                    onValueChange = { vm.updateHeadline(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Headline",
                    singleLine = true,
                    placeholder = "Give your news a headline",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomOutlinedTextField(
                    value = newsContent ?: TextFieldValue(""),
                    onValueChange = { vm.updateNewsContent(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "News Content",
                    placeholder = "News Content",
                    modifier = Modifier
                        .height((screenHeight * 0.2).dp)
                        .fillMaxWidth()
                )
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Change news image",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    CustomButton(
                        onClick = { newsImageGallery.launch("image/*") },
                        text = "Choose banner from gallery",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(0.dp)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Saved image",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        if (selectedImage != null) {
                            SelectedImageItem(uri = selectedImage)
                        } else {
                            Box(modifier = Modifier.size(100.dp))
                        }
                    }
                }
            }
            CustomButton(
                onClick = {
                    if (headline.text.isNotEmpty() && newsContent.text.isNotEmpty()) {
                        val changeMap = mapOf(
                            Pair("headline", headline!!.text),
                            Pair("newsContent", newsContent!!.text),
                        )
                        vm.updateNews(newsId = newsId, changeMap = changeMap)
                        selectedImage?.let {
                            vm.updateNewsImage(picUri = it, newsId = newsId)
                        }
                        scope.launch {
                            vm.updateLoadingStatus(true)
                            delay(1500)
                            vm.updateLoadingStatus(false)
                            onSave()
                        }
                    }
                },
                text = "Save",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = (screenHeight * 0.95).dp, max = (screenHeight * 0.95).dp)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(100.dp))
        }
    }

}

@Composable
fun NewsContent(
    vm: SingleScreenViewModel = viewModel(),
    news: News
) {
    val publisher by vm.publisher.observeAsState(null)
    var club: Club? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (club == null) {
            vm.getClub(news.clubId).get()
                .addOnSuccessListener { data ->
                    val fetchedClub = data.toObject(Club::class.java)
                    fetchedClub?.let { club = it }
                }
                .addOnFailureListener {
                    Log.e("FetchClub", "getClubFail: ", it)
                }
        }
        if (publisher == null) {
            vm.getPublisher(news.publisherId)
        }
    }
    club?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            AsyncImage(
                model = news.newsImageUri,
                contentDescription = "news image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(16f / 9f)
                    .padding(bottom = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(text = news.headline, fontSize = 20.sp)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = it.name, fontSize = 16.sp)
                }
                val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                val dateFormatted = sdf.format(news.date.toDate())
                Text(text = dateFormatted, fontWeight = FontWeight.Light, fontSize = 12.sp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = news.newsContent,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    publisher?.let {
                        Text(
                            text = "Published by ${it.fName} ${it.lName}",
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}