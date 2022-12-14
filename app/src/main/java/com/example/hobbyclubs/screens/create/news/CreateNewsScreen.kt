package com.example.hobbyclubs.screens.create.news

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.general.*
import com.google.firebase.Timestamp
import java.util.*


/**
 * Create news screen
 * This screen contains functions to create news.
 * @param navController for Compose navigation
 * @param vm [CreateNewsViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewsScreen(
    navController: NavController, vm: CreateNewsViewModel = viewModel()
) {
    val currentNewsCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = it.calculateBottomPadding()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            when (currentNewsCreationPage) {
                1 -> NewsCreationPage1(vm)
                2 -> NewsCreationPage2(vm, navController)
            }
        }
        CenterAlignedTopAppBar(title = { },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                TopBarBackButton(
                    navController = navController,
                    extraOnClick = { showLeaveDialog = true }
                )
            })
        if (showLeaveDialog) {
            CustomAlertDialog(
                onDismissRequest = { showLeaveDialog = false },
                onConfirm = {
                    navController.navigateUp()
                    showLeaveDialog = false
                },
                title = "Leave?",
                text = "Are you sure you want to leave? All information will be lost.",
                confirmText = "Leave"
            )
        }
        BackHandler(enabled = true) {
            showLeaveDialog = true
        }
    }
}

/**
 * Page progression
 * This composable contain the function of gray/blue bar over the next button.
 * @param numberOfLines
 * @param vm [CreateNewsViewModel]
 */
@Composable
fun PageProgression(numberOfLines: Int, vm: CreateNewsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProgressionBar(
            modifier = Modifier.weight(1f),
            isMarked = numberOfLines >= 1,
            onClick = { vm.changePageTo(1) })
        ProgressionBar(
            Modifier.weight(1f),
            isMarked = numberOfLines > 1,
            onClick = { vm.changePageTo(2) })
    }
}

/**
 * Progression bar
 * The bar turns blue to show you on which page you are and how many page left to create the news.
 * The bar is grey if you are not on the page.
 * @param modifier
 * @param isMarked
 * @param onClick
 */
@Composable
fun ProgressionBar(modifier: Modifier = Modifier, isMarked: Boolean, onClick: () -> Unit) {
    Box(modifier = modifier
        .height(13.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(color = if (isMarked) colorScheme.primary else colorScheme.surfaceVariant)
        .clickable { onClick() })
}

/**
 * News creation page1
 * This composable contains the content of first page ( headline, news content and club selection), and the check validation for the first screen.
 * @param vm [CreateNewsViewModel]
 */
@Composable
fun NewsCreationPage1(vm: CreateNewsViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val headline by vm.headline.observeAsState(null)
    val newsContent by vm.newsContent.observeAsState(null)
    val clubList by vm.clubsJoined.observeAsState(listOf())
    val selectedClub by vm.selectedClub.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Create a news!",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            CustomOutlinedTextField(
                value = headline ?: TextFieldValue(""),
                onValueChange = { vm.updateHeadline(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Headline *",
                singleLine = true,
                placeholder = "Give your news a headline *",
                modifier = Modifier.fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = newsContent ?: TextFieldValue(""),
                onValueChange = { vm.updateNewsContent(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "News Content *",
                placeholder = "News Content *",
                modifier = Modifier
                    .height((screenHeight * 0.4).dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.padding(10.dp))
            ClubSelectionDropdownMenu(clubList = clubList, onSelect = {
                vm.updateSelectedClub(it.ref)
            })
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            PageProgression(numberOfLines = 1, vm)
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (headline == null || newsContent == null || selectedClub == null) {
                        Toast.makeText(
                            context, "Please fill in all the fields", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        vm.changePageTo(2)
                    }
                },
            ) {
                Text(text = "Next")
            }
        }
    }
}

/**
 * News creation page2
 * This page contains the image selector and the button to create news, and the validation for the second screen.
 * @param vm [CreateNewsViewModel]
 * @param navController for Compose navigation
 */
@Composable
fun NewsCreationPage2(vm: CreateNewsViewModel, navController: NavController) {
    val context = LocalContext.current
    val selectedImage by vm.selectedImage.observeAsState(null)
    val headline by vm.headline.observeAsState(null)
    val newsContent by vm.newsContent.observeAsState(null)
    val selectedClub by vm.selectedClub.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ImagePicker(vm = vm)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            PageProgression(numberOfLines = 2, vm)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { vm.changePageTo(1) },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorScheme.onSurfaceVariant,
                        containerColor = colorScheme.surfaceVariant
                    ),
                ) {
                    Text(text = "Previous")
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (headline == null || newsContent == null || selectedClub == null) {
                            Toast.makeText(
                                context, "Please fill in all the fields", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val news = News(
                                clubId = selectedClub!!,
                                publisherId = FirebaseHelper.uid!!,
                                headline = headline!!.text,
                                newsContent = newsContent!!.text,
                                date = Timestamp.now()
                            )
                            val newsId = vm.addNews(news)
                            selectedImage?.let {
                                vm.storeNewsImage(it, newsId)
                            }
                            if (selectedClub!!.isNotEmpty()) {
                                vm.updateSingleNewsWithClubImageUri(
                                    clubId = selectedClub!!,
                                    newsId = newsId
                                )
                            }
                            Toast.makeText(context, "News created.", Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        }
                    },
                ) {
                    Text(text = "Create News")
                }
            }
        }
    }
}

/**
 * Image picker
 * This composable contains the function for image selector.
 * @param modifier
 * @param vm [CreateNewsViewModel]
 */
@Composable
fun ImagePicker(modifier: Modifier = Modifier, vm: CreateNewsViewModel) {
    val selectedImage by vm.selectedImage.observeAsState(null)
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.storeSelectedImage(uri)
        }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AsyncImage(
            model = selectedImage,
            contentScale = ContentScale.FillWidth,
            contentDescription = null,
            modifier = Modifier
                .padding(16.dp, 8.dp)
                .size(400.dp)
        )
    }
    Spacer(modifier = Modifier.padding(50.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        FilledTonalButton(
            modifier = Modifier
                .wrapContentSize()
                .padding(10.dp),
            onClick = { galleryLauncher.launch("image/*") }) {
            Text(text = if (selectedImage != null) "Re-select image" else "Add image")
        }
    }
}