package com.example.hobbyclubs.screens.clubmanagement

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.rememberModalBottomSheetState
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
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import kotlinx.coroutines.launch

/**
 * Club settings screen allows user to edit the existing club details
 *
 * @param navController for Compose navigation
 * @param clubId UID for the club you have selected on home or club screen
 * @param vm [ClubManagementViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ClubSettingsScreen(
    navController: NavController,
    clubId: String,
    vm: ClubManagementViewModel = viewModel()
) {

    val club by vm.selectedClub.observeAsState(null)
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val scope = rememberCoroutineScope()

    // content to show on the bottom sheet. defaults to empty surface.
    var sheetContent: @Composable () -> Unit by remember { mutableStateOf({ EmptySurface() }) }
    var showDeleteClubDialog by remember { mutableStateOf(false) }

    // if bottom sheet is open, close bottom sheet when user presses back button
    BackHandler(sheetState.isVisible) {
        scope.launch { sheetState.hide() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { sheetContent() },
        modifier = Modifier.fillMaxSize(),
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        // fetch club data
        LaunchedEffect(Unit) {
            vm.getClub(clubId)
        }
        // fill mutableLiveData with clubs previous details
        // for displaying previous data when opening sheets.
        LaunchedEffect(club) {
            club?.let {
                vm.fillPreviousClubData(it)
            }
        }
        club?.let { club ->
            Scaffold {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = it.calculateTopPadding()),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Club Settings",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                    )
                    ClubManagementSectionTitle(text = "Edit club details")
                    ClubSettingsRowItem(
                        text = "Name and Description",
                        onClick = {
                            sheetContent = {
                                NameAndDescriptionSheet(
                                    vm,
                                    clubId,
                                    onSave = { scope.launch { if (sheetState.isVisible) sheetState.hide() } })
                            }
                            scope.launch {
                                if (sheetState.isVisible) sheetState.hide()
                                else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ClubSettingsRowItem(
                        text = "Logo and Banner",
                        onClick = {
                            sheetContent = {
                                LogoAndBannerSheet(
                                    vm,
                                    clubId,
                                    onSave = { scope.launch { if (sheetState.isVisible) sheetState.hide() } })
                            }
                            scope.launch {
                                if (sheetState.isVisible) sheetState.hide()
                                else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ClubSettingsRowItem(
                        text = "Social Links",
                        onClick = {
                            sheetContent = {
                                SocialLinksSheet(
                                    vm,
                                    clubId,
                                    onSave = { scope.launch { if (sheetState.isVisible) sheetState.hide() } })
                            }
                            scope.launch {
                                if (sheetState.isVisible) sheetState.hide()
                                else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ClubSettingsRowItem(
                        text = "Contact Information",
                        onClick = {
                            sheetContent = {
                                ContactInfoSheet(
                                    vm,
                                    clubId,
                                    onSave = { scope.launch { if (sheetState.isVisible) sheetState.hide() } })
                            }
                            scope.launch {
                                if (sheetState.isVisible) sheetState.hide()
                                else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                            }
                        }
                    )
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        onClick = {
                            showDeleteClubDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError,
                        ),
                    ) {
                        Icon(Icons.Outlined.Delete, null)
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "Delete Club",
                        )
                    }
                }
                if (showDeleteClubDialog) {
                    CustomAlertDialog(
                        onDismissRequest = { showDeleteClubDialog = false },
                        onConfirm = {
                            scope.launch {
                                vm.deleteClub(clubId)
                                navController.navigate(NavRoute.Home.route)
                            }
                        },
                        title = "Delete Club?",
                        text = "Are you sure you want to delete this club? There is no going back.",
                        confirmText = "Delete"
                    )

                }
                CenterAlignedTopAppBar(
                    title = { Text(text = club.name, fontSize = 16.sp) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        TopBarBackButton(navController = navController)
                    }
                )
            }
        }
    }
}

/**
 * Club settings row item is a card that opens a specific bottom sheet.
 *
 * @param text Text to show on the card
 * @param onClick action to do when user taps on the card (open bottom sheet with correct content)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubSettingsRowItem(text: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        onClick = { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                modifier = Modifier
                    .weight(6f)
            )
            Icon(Icons.Outlined.NavigateNext, null, modifier = Modifier.size(24.dp))
        }
    }


}

/**
 * Empty surface for default value of bottom sheet
 */
@Composable
fun EmptySurface() {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {}
    }
}

/**
 * Name and description sheet for editing the the name and description of the club.
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 * @param onSave action to do when user wants to save the changes.
 */
@Composable
fun NameAndDescriptionSheet(vm: ClubManagementViewModel, clubId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val clubName by vm.clubName.observeAsState(TextFieldValue(""))
    val clubDescription by vm.clubDescription.observeAsState(TextFieldValue(""))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (screenHeight * 0.9).dp, max = (screenHeight * 0.9).dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                CreationPageTitle(
                    text = "Edit Name and Description",
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                CustomOutlinedTextField(
                    value = clubName ?: TextFieldValue(""),
                    onValueChange = { vm.updateClubName(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Name",
                    placeholder = "Give your club a name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
                CustomOutlinedTextField(
                    value = clubDescription ?: TextFieldValue(""),
                    onValueChange = { vm.updateClubDescription(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Description",
                    placeholder = "Describe your club",
                    modifier = Modifier
                        .height((screenHeight * 0.3).dp)
                        .fillMaxWidth()
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    if (clubName.text.isNotEmpty() && clubDescription.text.isNotBlank()) {
                        val changeMap = mapOf(
                            Pair("name", clubName!!.text),
                            Pair("description", clubDescription!!.text),
                        )
                        vm.updateClubDetails(clubId = clubId, changeMap)
                        onSave()
                    }
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}

/**
 * Logo and banner sheet for changing the logo and banner of the selected club.
 *
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 * @param onSave action to do when user wants to save the changes.
 */
@Composable
fun LogoAndBannerSheet(vm: ClubManagementViewModel, clubId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val selectedImage by vm.selectedBannerImage.observeAsState()
    val selectedLogo by vm.selectedClubLogo.observeAsState(null)

    // Launcher for Banner
    val galleryLauncherBanner =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.temporarilyStoreImages(bannerUri = uri)
        }
    // Launcher for Logo
    val galleryLauncherLogo =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            vm.temporarilyStoreImages(logoUri = uri)
        }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (screenHeight * 0.9).dp, max = (screenHeight * 0.9).dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.wrapContentSize()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CreationPageTitle(
                        text = "Add a club logo",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { galleryLauncherLogo.launch("image/*") },
                    ) {
                        Text(text = "Choose logo from gallery")
                    }
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
                            SelectedImageItem(uri = it, onDelete = { vm.removeSelectedImage(logo = true) })
                        } ?:  Box(modifier = Modifier.size(110.dp))
                    }
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    CreationPageTitle(
                        text = "Add images about the club",
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { galleryLauncherBanner.launch("image/*") },
                    ) {
                        Text(text = "Choose images from gallery")
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    ) {
                        CreationPageSubtitle(text = "Saved images", modifier = Modifier.padding(bottom = 20.dp))
                        selectedImage?.let { uri ->
                            SelectedImageItem(uri = uri, onDelete = { vm.removeSelectedImage(banner = true) })
                        } ?: Box(modifier = Modifier.size(110.dp))
                    }
                }
            }
            Button(
                onClick = {
                    if (selectedLogo != null && selectedImage != null) {
                        vm.replaceClubImage(
                            clubId = clubId,
                            bannerUri = selectedImage!!,
                            logoUri = selectedLogo!!
                        )
                        onSave()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(text = "Save")
            }
        }
    }
}

/**
 * Social links sheet for adding new links to the club.
 *
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 * @param onSave action to do when user wants to save the changes.
 */
@Composable
fun SocialLinksSheet(vm: ClubManagementViewModel, clubId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var linkSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinksLiveData.observeAsState(mapOf())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (screenHeight * 0.9).dp, max = (screenHeight * 0.9).dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
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
                FilledTonalButton(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
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
                ) {
                    Text(text = "Add Link")
                }
                Text(
                    text = "Provided links",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                givenLinks.forEach {
                    Text(text = it.key)
                }
                // change focus after pressed the add link button
                DisposableEffect(linkSent) {
                    if (linkSent) {
                        focusRequester.requestFocus()
                    }
                    onDispose {}
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    if (givenLinks != null) {
                        val changeMap = mapOf(
                            Pair("socials", givenLinks)
                        )
                        vm.updateClubDetails(clubId = clubId, changeMap)
                        onSave()
                    }
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}

/**
 * Contact info sheet for editing the contact information of the club.
 *
 * @param vm [ClubManagementViewModel]
 * @param clubId UID for the club you have selected on home or club screen
 * @param onSave action to do when user wants to save the changes.
 */
@Composable
fun ContactInfoSheet(vm: ClubManagementViewModel, clubId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current

    val contactInfoName by vm.contactInfoName.observeAsState(TextFieldValue(""))
    val contactInfoEmail by vm.contactInfoEmail.observeAsState(TextFieldValue(""))
    val contactInfoNumber by vm.contactInfoNumber.observeAsState(TextFieldValue(""))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (screenHeight * 0.9).dp, max = (screenHeight * 0.9).dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
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
                        .padding(bottom = 10.dp)
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
                        .padding(bottom = 10.dp)
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
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (contactInfoName.text.isNotBlank() && contactInfoEmail.text.isNotBlank() && contactInfoNumber.text.isNotBlank()) {
                        val changeMap = mapOf(
                            Pair("contactPerson", contactInfoName.text),
                            Pair("contactEmail", contactInfoEmail.text),
                            Pair("contactPhone", contactInfoNumber.text)
                        )
                        vm.updateClubDetails(clubId, changeMap)
                        onSave()
                    }
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}

