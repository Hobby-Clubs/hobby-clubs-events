package com.example.hobbyclubs.screens.create.club

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.CustomAlertDialog
import com.example.hobbyclubs.general.CustomOutlinedTextField
import com.example.hobbyclubs.general.Pill
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.example.hobbyclubs.screens.create.event.*
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    navController: NavController,
    vm: CreateClubViewModel = viewModel()
) {
    val currentClubCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            when (currentClubCreationPage) {
                1 -> ClubCreationPage1(vm)
                2 -> ClubCreationPage2(vm)
                3 -> ClubCreationPage3(vm)
                4 -> ClubCreationPage4(vm, navController)
            }
        }
        CenterAlignedTopAppBar(
            title = { },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                IconButton(onClick = { showLeaveDialog = true }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )
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

@Composable
fun ClubCreationPage1(vm: CreateClubViewModel) {
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val clubName by vm.clubName.observeAsState(null)
    val clubDescription by vm.clubDescription.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Create a Nokia hobby club!\nUnite People!\nShare Interests!",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            CustomOutlinedTextField(
                value = clubName ?: TextFieldValue(""),
                onValueChange = { vm.updateEventName(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Name",
                placeholder = "Give your club a name",
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = clubDescription ?: TextFieldValue(""),
                onValueChange = { vm.updateEventDescription(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Description",
                placeholder = "Describe your club",
                modifier = Modifier
                    .height((screenHeight * 0.3).dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            SelectPrivacy(vm)
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(
                numberOfLines = 1,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            CustomButton(
                onClick = { vm.changePageTo(2) },
                text = "Next",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ClubCreationPage2(vm: CreateClubViewModel) {
    val context = LocalContext.current
    val selectedImages by vm.selectedBannerImages.observeAsState(mutableListOf())
    val selectedLogo by vm.selectedClubLogo.observeAsState(Uri.EMPTY)
    val selectedImagesAsBitmap by vm.imagesAsBitmap.observeAsState(listOf())
    val selectedLogoAsBitmap by vm.logoAsBitmap.observeAsState(null)
    var showBannerImagesPreview by remember { mutableStateOf(false) }
    var showLogoPreview by remember { mutableStateOf(false) }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            vm.temporarilyStoreImages(bannerUri = uriList.toMutableList())
            showBannerImagesPreview = true
        }
    val galleryLauncherLogo =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.temporarilyStoreImages(logoUri = uri)
            showLogoPreview = true
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.wrapContentSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add a club logo",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                CustomButton(
                    onClick = { galleryLauncherLogo.launch("image/*") },
                    text = "Choose logo from gallery",
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
                        text = "Saved logo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    selectedLogoAsBitmap?.let {
                        SelectedImageItem(bitmap = it)
                    }
                    if (selectedLogoAsBitmap == null) {
                        Box(modifier = Modifier.size(100.dp))
                    }
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add images about the club",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                CustomButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    text = "Choose images from gallery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Text(
                        text = "Saved images",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    val pagerState = rememberPagerState()
                    if (selectedImagesAsBitmap.isNotEmpty()) {
                        HorizontalPager(
                            count = selectedImagesAsBitmap.size,
                            state = pagerState,
                            itemSpacing = 10.dp,
                            contentPadding = PaddingValues(end = 200.dp)
                        ) { page ->
                            Log.d("imageList", "page: $page, index: ${selectedImagesAsBitmap[page]}")
                            SelectedImageItem(bitmap = selectedImagesAsBitmap[page])
                        }
                        HorizontalPagerIndicator(
                            pagerState = pagerState,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp),
                        )
                    } else {
                        Box(modifier = Modifier.size(100.dp))
                    }

                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(
                numberOfLines = 2,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(1) },
                    text = "Previous",
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .height(60.dp)
                )
                CustomButton(
                    onClick = { vm.changePageTo(3) },
                    text = "Next",
                    modifier = Modifier
                        .height(60.dp)
                )
            }
        }
        if (showBannerImagesPreview) {
            SelectedImagesDialog(
                selectedImages = selectedImages,
                onConfirm = {
                    vm.convertUriToBitmap(bannerImages = selectedImages, context = context)
                    vm.emptySelection(banner = true)
                    showBannerImagesPreview = false
                },
                onDismissRequest = { showBannerImagesPreview = false })
        }
        if (showLogoPreview) {
            SelectedImagesDialog(
                selectedLogo = selectedLogo,
                onConfirm = {
                    vm.convertUriToBitmap(logo = selectedLogo, context =  context)
                    vm.emptySelection(logo = true)
                    showLogoPreview = false
                },
                onDismissRequest = { showLogoPreview = false })
        }
    }
}

@Composable
fun ClubCreationPage3(vm: CreateClubViewModel) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var linkSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinksLiveData.observeAsState(mapOf())

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Add social media links",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Give people your community links (eg. Facebook, Discord, Twitter)",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            CustomOutlinedTextField(
                value = currentLinkName ?: TextFieldValue(""),
                onValueChange = { vm.updateCurrentLinkName(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Link name",
                placeholder = "Name your link",
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            CustomOutlinedTextField(
                value = currentLinkURL ?: TextFieldValue(""),
                onValueChange = { vm.updateCurrentLinkURL(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Link",
                placeholder = "Link (URL)",
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomButton(
                onClick = {
                    if (currentLinkName != null && currentLinkURL != null) {
                        vm.addLinkToList(
                            Pair(
                                currentLinkName!!.text.replaceFirstChar { it.uppercase() },
                                currentLinkURL!!.text
                            )
                        )
                        vm.clearLinkFields()
                        linkSent = true
                    } else {
                        Toast.makeText(context, "Please fill both fields.", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                text = "Add Link",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            )
            Text(
                text = "Provided links",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            givenLinks.forEach {
                Text(text = it.key)
            }
            DisposableEffect(linkSent) {
                if (linkSent) {
                    focusRequester.requestFocus()
                }
                onDispose {}
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(
                numberOfLines = 3,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(2) },
                    text = "Previous",
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .height(60.dp)
                )
                CustomButton(
                    onClick = { vm.changePageTo(4) },
                    text = "Next",
                    modifier = Modifier
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
fun ClubCreationPage4(vm: CreateClubViewModel, navController: NavController) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val clubName by vm.clubName.observeAsState(null)
    val clubDescription by vm.clubDescription.observeAsState(null)
    val clubIsPrivate by vm.clubIsPrivate.observeAsState(false)
    val linkArray by vm.givenLinksLiveData.observeAsState(null)
    val currentUser by vm.currentUser.observeAsState()
    val selectedImages by vm.imagesAsBitmap.observeAsState()
    val selectedLogoAsBitmap by vm.logoAsBitmap.observeAsState()

    // Last Page
    val contactInfoName by vm.contactInfoName.observeAsState(null)
    val contactInfoEmail by vm.contactInfoEmail.observeAsState(null)
    val contactInfoNumber by vm.contactInfoNumber.observeAsState(null)

    val scope = rememberCoroutineScope()

    if (currentUser == null) {
        LaunchedEffect(Unit) {
            scope.launch {
                vm.getCurrentUser()
                Log.d("fetchUser", "current user fetched")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Contact Information",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Provide members a way to contact you directly",
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            CustomOutlinedTextField(
                value = contactInfoName ?: TextFieldValue(""),
                onValueChange = { vm.updateContactInfoName(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Name",
                placeholder = "Name",
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = contactInfoEmail ?: TextFieldValue(""),
                onValueChange = { vm.updateContactInfoEmail(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Email,
                label = "Email",
                placeholder = "Email",
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = contactInfoNumber ?: TextFieldValue(""),
                onValueChange = { vm.updateContactInfoNumber(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Number,
                label = "Phone number",
                placeholder = "Phone number",
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Or fill quickly using account details", fontSize = 12.sp)
            CustomButton(
                onClick = { currentUser?.let { vm.quickFillOptions(it) } },
                text = "Quick fill"
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(
                numberOfLines = 4,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(3) },
                    text = "Previous",
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.LightGray
                    ),
                    modifier = Modifier
                        .height(60.dp)
                )
                CustomButton(
                    onClick = {
                        if (
                            clubName == null ||
                            clubDescription == null ||
                            clubIsPrivate == null ||
                            contactInfoName == null ||
                            contactInfoEmail == null ||
                            contactInfoNumber == null
                        ) {
                            Toast.makeText(
                                context,
                                "Please fill in all the fields",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@CustomButton
                        } else {
                            val listWithYouAsAdminAndParticipant = mutableListOf<String>()
                            FirebaseHelper.uid?.let {
                                listWithYouAsAdminAndParticipant.add(it)
                            }
                            val club = Club(
                                name = clubName!!.text,
                                description = clubDescription!!.text,
                                isPrivate = clubIsPrivate,
                                admins = listWithYouAsAdminAndParticipant,
                                members = listWithYouAsAdminAndParticipant,
                                socials = if (linkArray == null || linkArray!!.isEmpty()) mapOf() else linkArray!!,
                                contactPerson = contactInfoName!!.text,
                                contactEmail = contactInfoEmail!!.text,
                                contactPhone = contactInfoNumber!!.text
                            )
                            val clubId = vm.addClub(club)
                            vm.storeBitmapsOnFirebase(
                                listToStore = selectedImages?.toList() ?: listOf(),
                                logo = selectedLogoAsBitmap ?: BitmapFactory.decodeResource(context.resources, R.drawable.nokia_logo),
                                clubId = clubId
                            )
                            Toast.makeText(context, "Event created.", Toast.LENGTH_SHORT).show()
                            navController.navigate(NavRoutes.HomeScreen.route)
                        }
                    },
                    text = "Create Club",
                    modifier = Modifier
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
fun SelectPrivacy(vm: CreateClubViewModel) {
    val leftSelected by vm.leftSelected.observeAsState(true)
    val rightSelected by vm.rightSelected.observeAsState(false)
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Privacy",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Pill(modifier = Modifier.weight(1f), isSelected = leftSelected, text = "Public") {
                vm.updateClubPrivacySelection(leftVal = true, rightVal = false)
            }
            Pill(
                modifier = Modifier.weight(1f),
                isLeft = false,
                isSelected = rightSelected,
                text = "Private"
            ) {
                vm.updateClubPrivacySelection(leftVal = false, rightVal = true)
            }
        }

    }
}
