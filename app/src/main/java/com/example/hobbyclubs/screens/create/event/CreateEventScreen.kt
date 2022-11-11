package com.example.hobbyclubs.screens.create.event

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.compose.nokiaLighterBlue
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    vm: CreateEventViewModel = viewModel()
) {
    val currentEventCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(75.dp))
            when (currentEventCreationPage) {
                1 -> EventCreationPage1(vm)
                2 -> EventCreationPage2(vm)
                3 -> EventCreationPage3(vm)
                4 -> EventCreationPage4(vm, navController)
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
                }
            )
        }
        BackHandler(enabled = true) {
            showLeaveDialog = true
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        label = { Text(text = label) },
        placeholder = { Text(text = placeholder) },
        modifier = modifier
    )
}

@Composable
fun PageProgression(numberOfLines: Int, vm: CreateEventViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressionBar(isMarked = numberOfLines >= 1, onClick = { vm.changePageTo(1) })
        ProgressionBar(isMarked = numberOfLines > 1, onClick = { vm.changePageTo(2) })
        ProgressionBar(isMarked = numberOfLines > 2, onClick = { vm.changePageTo(3) })
        ProgressionBar(isMarked = numberOfLines > 3, onClick = { vm.changePageTo(4) })
    }
}

@Composable
fun ProgressionBar(isMarked: Boolean, onClick: () -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Box(modifier = Modifier
        .width((screenWidth * 0.21).dp)
        .height(13.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(color = if (isMarked) nokiaLighterBlue else Color.LightGray)
        .clickable { onClick() }
    )
}

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(text = "Leave?") },
        text = { Text(text = "Are you sure you want to leave? All information will be lost.") },
        onDismissRequest = onDismissRequest,
        dismissButton = {
            Button(onClick = { onDismissRequest() }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Leave")
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
@Composable
fun SelectedImagesDialog(
    vm: CreateEventViewModel,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val selectedImages by vm.selectedImages.observeAsState(mutableListOf())
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(5.dp)
                ) {
                    Text(text = "Image Previews")
                    Spacer(modifier = Modifier.height(10.dp))
                    val pagerState2 = rememberPagerState()
                    HorizontalPager(
                        count = selectedImages.size,
                        state = pagerState2,
                        itemSpacing = 10.dp,
                        contentPadding = PaddingValues(end = 150.dp)
                    ) { page ->
                        Log.d("imageList", "page: $page, index: ${selectedImages[page]}")
                        SelectedImageItem(uri = selectedImages[page])
                    }
                    HorizontalPagerIndicator(
                        pagerState = pagerState2,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        CustomButton(
                            onClick = { onDismissRequest() },
                            text = "Cancel",
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.LightGray,
                                contentColor = Color.Black
                            )
                        )
                        CustomButton(onClick = { onConfirm() }, text = "Save Selection")
                    }

                }
            }
        }
    }
}

@Composable
fun SelectedImageItem(bitmap: Bitmap? = null, uri: Uri? = null) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.height(150.dp),
            contentScale = ContentScale.FillHeight
        )
    }
    if (uri != null) {
        Image(
            painter = rememberAsyncImagePainter(uri),
            contentDescription = null,
            modifier = Modifier.height(150.dp),
            contentScale = ContentScale.FillHeight
        )
    }

}

@Composable
fun ClubSelectionDropdownMenu(vm: CreateEventViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val items = mutableListOf("Club1", "Club2", "Club3")
    var selectedIndex: Int? by remember { mutableStateOf(null) }

    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { expanded = true }
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(BorderStroke(1.dp, Color.Black))
            .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = if (selectedIndex != null) items[selectedIndex!!] else "Select Club", modifier = Modifier.weight(6f), textAlign = TextAlign.Start)
                Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.weight(1f))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White)
        ) {
            items.forEachIndexed { index, club ->
                DropdownMenuItem(
                    text = { Text(text = club) },
                    onClick = {
                        selectedIndex = index
                        vm.updateSelectedClub(club)
                        expanded = false
                    }
                )
            }
        }
    }

}

@Composable
fun DateSelector(vm: CreateEventViewModel) {
    val context = LocalContext.current

    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()

    val selectedDate by vm.selectedDate.observeAsState("Select Date")

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, dayOfMonth: Int ->
            vm.updateSelectedDate("$dayOfMonth/${mMonth+1}/$mYear")
        }, year, month, day
    )

    Box(modifier = Modifier
        .fillMaxWidth()
        .clickable { datePickerDialog.show() }
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(BorderStroke(1.dp, Color.Black))
            .padding(horizontal = 15.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = selectedDate, modifier = Modifier.weight(6f), textAlign = TextAlign.Start)
                Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun EventCreationPage1(vm: CreateEventViewModel) {
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val eventName by vm.eventName.observeAsState(null)
    val eventDescription by vm.eventDescription.observeAsState(null)
    val eventLocation by vm.eventLocation.observeAsState(null)
    val eventParticipantLimit by vm.eventParticipantLimit.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Create a new event!",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            ClubSelectionDropdownMenu(vm)
            CustomOutlinedTextField(
                value = eventName ?: TextFieldValue(""),
                onValueChange = { vm.updateEventName(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Name",
                placeholder = "Give your event a name",
                modifier = Modifier
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = eventDescription ?: TextFieldValue(""),
                onValueChange = { vm.updateEventDescription(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Description",
                placeholder = "Describe your event",
                modifier = Modifier
                    .height((screenHeight * 0.2).dp)
                    .fillMaxWidth()
            )
            CustomOutlinedTextField(
                value = eventLocation ?: TextFieldValue(""),
                onValueChange = { vm.updateEventLocation(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Location",
                placeholder = "Give the address of the event",
                modifier = Modifier
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))
            DateSelector(vm)
            Spacer(modifier = Modifier.height(5.dp))
            CustomOutlinedTextField(
                value = eventParticipantLimit ?: TextFieldValue(""),
                onValueChange = { vm.updateEventParticipantLimit(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Number,
                label = "Participant limit",
                placeholder = "Leave empty for no limit",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(numberOfLines = 1, vm)
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
fun EventCreationPage2(vm: CreateEventViewModel) {

    val context = LocalContext.current
    val selectedImages by vm.selectedImages.observeAsState(mutableListOf())
    val selectedImagesAsBitmap by vm.imagesAsBitmap.observeAsState(listOf())
    var showImagePreview by remember { mutableStateOf(false) }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            vm.temporarilyStoreImages(uriList.toMutableList())
            showImagePreview = true
        }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Add images about the event",
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
                        contentPadding = PaddingValues(end = 150.dp)
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
                    Box(modifier = Modifier.size(150.dp))
                }

            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(numberOfLines = 2, vm)
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
        if (showImagePreview) {
            SelectedImagesDialog(
                vm = vm,
                onConfirm = {
                    vm.convertUriToBitmap(selectedImages, context)
                    vm.emptySelection()
                    showImagePreview = false
                },
                onDismissRequest = { showImagePreview = false })
        }
    }
}

@Composable
fun EventCreationPage3(vm: CreateEventViewModel) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var linkSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinksLiveData.observeAsState(mutableListOf())

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
                Text(text = it.first)
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
            PageProgression(numberOfLines = 3, vm)
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
fun EventCreationPage4(vm: CreateEventViewModel, navController: NavController) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // First page
    val selectedClub by vm.selectedClub.observeAsState(null)
    val selectedDate by vm.selectedDate.observeAsState(null)
    val eventName by vm.eventName.observeAsState(null)
    val eventDescription by vm.eventDescription.observeAsState(null)
    val eventLocation by vm.eventLocation.observeAsState(null)
    val eventParticipantLimit by vm.eventParticipantLimit.observeAsState(null)
    val linkArray by vm.givenLinksLiveData.observeAsState(null)
    val currentUser by vm.currentUser.observeAsState()

    val selectedImages by vm.imagesAsBitmap.observeAsState()

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
                text = "Quick fill")
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 75.dp)
        ) {
            PageProgression(numberOfLines = 4, vm)
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
                            selectedClub == null ||
                            selectedDate == null ||
                            eventName == null ||
                            eventDescription == null ||
                            eventLocation == null ||
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
                            val event = Event(
                                clubId = selectedClub!!,
                                name = eventName!!.text,
                                description = eventDescription!!.text,
                                date = selectedDate!!,
                                address = eventLocation!!.text,
                                participantLimit = if (eventParticipantLimit == null || eventParticipantLimit!!.text.isBlank()) -1 else eventParticipantLimit!!.text.toInt(),
                                linkArray = if (linkArray == null || linkArray!!.size == 0) mutableListOf() else linkArray!!,
                                contactInfoName = contactInfoName!!.text,
                                contactInfoEmail = contactInfoEmail!!.text,
                                contactInfoNumber = contactInfoNumber!!.text
                            )
                            val eventId = vm.addEvent(event)
                            vm.storeBitmapsOnFirebase(listToStore = selectedImages?.toList() ?: listOf(), eventId = eventId)
                            Toast.makeText(context, "Event created.", Toast.LENGTH_SHORT).show()
                            navController.navigate(NavRoutes.HomeScreen.route)
                        }
                    },
                    text = "Create Event",
                    modifier = Modifier
                        .height(60.dp)
                )
            }
        }
    }
}
