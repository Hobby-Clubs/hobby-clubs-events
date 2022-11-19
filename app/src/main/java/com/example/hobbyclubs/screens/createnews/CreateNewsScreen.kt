package com.example.hobbyclubs.screens.createnews

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.example.hobbyclubs.screens.create.event.ClubSelectionDropdownMenu
import com.google.firebase.Timestamp
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewsScreen(
    navController: NavController, vm: CreateNewsViewModel = viewModel()
) {
    val currentNewsCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
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
                IconButton(onClick = { showLeaveDialog = true }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            })
        if (showLeaveDialog) {
            CustomAlertDialog(onDismissRequest = { showLeaveDialog = false }, onConfirm = {
                navController.navigateUp()
                showLeaveDialog = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusManager: FocusManager,
    keyboardType: KeyboardType,
    label: String,
    placeholder: String
) {
    OutlinedTextField(value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done, keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        modifier = modifier
    )
}

@Composable
fun PageProgression(numberOfLines: Int, vm: CreateNewsViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ProgressionBar(isMarked = numberOfLines >= 1, onClick = { vm.changePageTo(1) })
        ProgressionBar(isMarked = numberOfLines > 1, onClick = { vm.changePageTo(2) })
    }
}

@Composable
fun ProgressionBar(isMarked: Boolean, onClick: () -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box(modifier = Modifier
        .width((screenWidth * 0.21).dp)
        .height(13.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(color = if (isMarked) MaterialTheme.colorScheme.primary else Color.LightGray)
        .clickable { onClick() })
}

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit, onConfirm: () -> Unit
) {
    AlertDialog(title = { Text(text = "Leave?") },
        text = { Text(text = "Are you sure you want to leave? All information will be lost.") },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            Button(onClick = { onDismissRequest() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red, contentColor = Color.White
                )
            ) {
                Text(text = "Leave")
            }
        })
}

@Composable
fun NewsCreationPage1(vm: CreateNewsViewModel) {
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val headline by vm.headline.observeAsState(null)
    val newsContent by vm.newsContent.observeAsState(null)
    val clubList by vm.clubsJoined.observeAsState(listOf())

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
                label = "Headline",
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
                .padding(bottom = 75.dp)
        ) {
            PageProgression(numberOfLines = 1, vm)
            Button(onClick = { vm.changePageTo(2) },
                shape = RoundedCornerShape(
                    topStartPercent = 25,
                    bottomStartPercent = 25,
                    topEndPercent = 25,
                    bottomEndPercent = 25
                ),) {
                Text(
                    text = "Next",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}

@Composable
fun NewsCreationPage2(vm: CreateNewsViewModel, navController: NavController) {
    val context = LocalContext.current
    val selectImages by vm.selectedImage.observeAsState(null)
    val selectImageBitmap by vm.selectedImageBitmap.observeAsState(null)
    // First page
    val headline by vm.headline.observeAsState(null)
    val newsContent by vm.newsContent.observeAsState(null)
    val selectedClub by vm.selectedClub.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ImagePicker(vm)
            PageProgression(numberOfLines = 2, vm)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly)
            {
               Button(modifier = Modifier,
                   onClick = { vm.changePageTo(1)
               },
                   shape = RoundedCornerShape(
                       topStartPercent = 25,
                   bottomStartPercent = 25
               )) {
                    Text(
                        text = "Previous", modifier = Modifier
                            .height(60.dp)
                            .width(150.dp)
                            .wrapContentHeight(),
                        textAlign = TextAlign.Start
                    )
                }
                Button(
                    modifier = Modifier,
                    onClick = {
                        vm.convertUriToBitmap(selectImages!!, context)
                        if (headline == null || newsContent == null || selectedClub == null || selectImageBitmap == null) {
                            Log.d("imageStoring", "$headline\n$newsContent\n$selectImageBitmap")
                            Toast.makeText(
                                context, "Please fill in all the fields", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val news = News(
                                clubId = selectedClub!!,
                                headline = headline!!.text,
                                newsContent = newsContent!!.text,
                                date = Timestamp.now()
                            )
                            val newsId = vm.addNews(news)
                            vm.storeNewsImage(bitmap = selectImageBitmap!!, newsId)
                            Toast.makeText(context, "News created.", Toast.LENGTH_SHORT).show()
                            navController.navigate(NavRoutes.HomeScreen.route)
                        }
                    },
                    shape = RoundedCornerShape(
                        topEndPercent = 25,
                        bottomEndPercent = 25
                    ),
                )
                {
                    Text(
                        text = "Create News",
                        modifier = Modifier
                            .height(60.dp)
                            .width(150.dp)
                            .wrapContentHeight(),
                        textAlign = TextAlign.End
                    )

                }
            }
        }
    }
}

@Composable
fun ImagePicker(vm: CreateNewsViewModel) {

    val selectImages by vm.selectedImage.observeAsState(null)

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.storeSelectedImage(uri)
        }
        Column( modifier= Modifier
            .height(400.dp)
            .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

                    Image(
                        painter = rememberAsyncImagePainter(selectImages),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(16.dp, 8.dp)
                            .size(400.dp)
                            .clickable {

                            }
                    )
            }
            Spacer(modifier = Modifier.padding(50.dp))
    Row(modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center) {

        CustomButton(
            onClick = { galleryLauncher.launch("image/*") },
            text = "Add Image",
            modifier = Modifier
                .wrapContentSize()
                .padding(10.dp)
        )
    }

        }


