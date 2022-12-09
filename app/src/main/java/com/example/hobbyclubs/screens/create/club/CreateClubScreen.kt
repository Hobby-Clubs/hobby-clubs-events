package com.example.hobbyclubs.screens.create.club

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
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
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.create.event.*
import kotlinx.coroutines.launch

/**
 * Create club screen allows the user to create new club for their community.
 * @param navController for Compose navigation
 * @param vm [CreateClubViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClubScreen(
    navController: NavController,
    vm: CreateClubViewModel = viewModel()
) {
    val currentClubCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Scaffold() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = it.calculateBottomPadding()),
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
                TopBarBackButton(navController = navController)
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

/**
 * Club creation page 1 asks the user to select:
 * - Name and description
 * - Privacy
 *
 * @param vm [CreateClubViewModel]
 */
@Composable
fun ClubCreationPage1(vm: CreateClubViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp

    // Club details
    val clubName by vm.clubName.observeAsState(null)
    val clubDescription by vm.clubDescription.observeAsState(null)
    val clubIsPrivate by vm.clubIsPrivate.observeAsState(false)

    // for privacy selection
    val publicSelected by vm.publicSelected.observeAsState(true)
    val privateSelected by vm.privateSelected.observeAsState(false)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreationPageTitle(
                text = "Create a Nokia hobby club!\nUnite People!\nShare Interests!",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            CustomOutlinedTextField(
                value = clubName ?: TextFieldValue(""),
                onValueChange = { vm.updateClubName(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Name *",
                placeholder = "Give your club a name *",
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = clubDescription ?: TextFieldValue(""),
                onValueChange = { vm.updateClubDescription(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Description *",
                placeholder = "Describe your club *",
                modifier = Modifier
                    .height((screenHeight * 0.3).dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            SelectPrivacy(
                selectedPublic = publicSelected,
                selectedPrivate = privateSelected,
                onClickPublic = { vm.updateClubPrivacySelection(leftVal = true, rightVal = false) },
                onClickPrivate = { vm.updateClubPrivacySelection(leftVal = false, rightVal = true) }
            )
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
                onClick = {
                    if (
                        clubName == null ||
                        clubDescription == null ||
                        clubIsPrivate == null
                    ) {
                        Toast.makeText(
                            context,
                            "Please fill in all the fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        vm.changePageTo(2)
                    }
                },
                text = "Next",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

/**
 * Club creation page 2 asks the user to select:
 * - Club Logo
 * - Club Banner
 *
 * @param vm [CreateClubViewModel]
 */
@Composable
fun ClubCreationPage2(vm: CreateClubViewModel) {
    val selectedBanner by vm.selectedBannerImage.observeAsState(null)
    val selectedLogo by vm.selectedClubLogo.observeAsState(null)

    // Launchers for selecting images from devices storage.
    // Returns a uri for the image you have selected.
    val galleryLauncherBanner =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uriList ->
            vm.temporarilyStoreImages(bannerUri = uriList)
        }
    val galleryLauncherLogo =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.temporarilyStoreImages(logoUri = uri)
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.wrapContentSize()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CreationPageTitle(
                    text = "Add a club logo",
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
                    selectedLogo?.let {
                        SelectedImageItem(
                            uri = selectedLogo,
                            onDelete = { vm.removeSelectedImage(logo = true) })
                    } ?: Box(modifier = Modifier.size(110.dp))
                    // just to take the space that the image would take height-wise
                }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                CreationPageTitle(
                    text = "Add image about the club",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                CustomButton(
                    onClick = { galleryLauncherBanner.launch("image/*") },
                    text = "Choose image from gallery",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    CreationPageSubtitle(
                        text = "Saved images",
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    selectedBanner?.let {
                        SelectedImageItem(
                            uri = selectedBanner,
                            onDelete = { vm.removeSelectedImage(banner = true) })
                    } ?: Box(modifier = Modifier.size(100.dp))
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
                        contentColor = colorScheme.onSurfaceVariant,
                        containerColor = colorScheme.surfaceVariant
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
    }
}

/**
 * Club creation page 3 asks the user to provide:
 * - Link name
 * - Url for that link
 *
 * @param vm [CreateClubViewModel]
 */
@Composable
fun ClubCreationPage3(vm: CreateClubViewModel) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    // for changing focus after link added
    var linkSent by remember { mutableStateOf(false) }

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinksLiveData.observeAsState(mapOf())

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CreationPageTitle(text = "Add social media links")
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
            CreationPageSubtitle(
                text = "Provided links",
                modifier = Modifier.padding(bottom = 20.dp)
            )
            givenLinks.forEach {
                Text(text = it.key)
            }
            // when link button pressed then change focus back on link name text field
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
                        contentColor = colorScheme.onSurfaceVariant,
                        containerColor = colorScheme.surfaceVariant
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

/**
 * Club creation page 4 asks the user to provide:
 * - Contact information
 *
 * @param vm [CreateClubViewModel]
 * @param navController for Compose navigation
 */
@Composable
fun ClubCreationPage4(vm: CreateClubViewModel, navController: NavController) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // First page
    val clubName by vm.clubName.observeAsState(null)
    val clubDescription by vm.clubDescription.observeAsState(null)
    val clubIsPrivate by vm.clubIsPrivate.observeAsState(false)

    // Second Page
    val selectedImage by vm.selectedBannerImage.observeAsState()
    val selectedLogo by vm.selectedClubLogo.observeAsState()

    // Third Page
    val linkArray by vm.givenLinksLiveData.observeAsState(null)

    // Last Page
    val currentUser by vm.currentUser.observeAsState()
    val contactInfoName by vm.contactInfoName.observeAsState(null)
    val contactInfoEmail by vm.contactInfoEmail.observeAsState(null)
    val contactInfoNumber by vm.contactInfoNumber.observeAsState(null)
    val scope = rememberCoroutineScope()

    // fetch details for the current user for quick fill options
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            scope.launch {
                vm.getCurrentUser()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CreationPageTitle(text = "Contact Information")
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
                        contentColor = colorScheme.onSurfaceVariant,
                        containerColor = colorScheme.surfaceVariant
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
                            val defaultUri =
                                Uri.parse("android.resource://com.example.hobbyclubs/drawable/nokia_logo.png")
                            vm.storeImagesOnFirebase(
                                bannerUri = selectedImage ?: defaultUri,
                                logoUri = selectedLogo ?: defaultUri,
                                clubId = clubId
                            )
                            Toast.makeText(context, "Club created.", Toast.LENGTH_SHORT).show()
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