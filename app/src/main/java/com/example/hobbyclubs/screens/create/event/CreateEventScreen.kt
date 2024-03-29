package com.example.hobbyclubs.screens.create.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Timer
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Create event screen allows the user to create new events for your own club
 * or create a general event for everyone.
 * @param navController for Compose navigation
 * @param vm [CreateEventViewModel]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    navController: NavController,
    vm: CreateEventViewModel = viewModel()
) {
    val currentEventCreationPage by vm.currentCreationProgressPage.observeAsState(1)
    var showLeaveDialog by remember { mutableStateOf(false) }
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = it.calculateBottomPadding()),
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
                TopBarBackButton(
                    navController = navController,
                    extraOnClick = { showLeaveDialog = true }
                )
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
 * Date selector allows user to select a date and time for the event
 * @param vm [CreateEventViewModel]
 */
@Composable
fun DateSelector(vm: CreateEventViewModel) {
    val context = LocalContext.current

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    calendar.time = Date()

    val selectedDate by vm.selectedDate.observeAsState()
    val selectedYear = remember { mutableStateOf(0) }
    val selectedMonth = remember { mutableStateOf(0) }
    val selectedDay = remember { mutableStateOf(0) }
    val selectedHour = remember { mutableStateOf(0) }
    val selectedMinute = remember { mutableStateOf(0) }

    // Dialog for picking date
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, dayOfMonth: Int ->
            selectedYear.value = mYear
            selectedMonth.value = mMonth
            selectedDay.value = dayOfMonth
        },
        year, month, day,
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    // Dialog for picking a time
    val timePickerDialog = TimePickerDialog(
        context,
        3,
        { _, mHour: Int, mMinute: Int ->
            selectedHour.value = mHour
            selectedMinute.value = mMinute
            vm.updateSelectedDate(
                years = selectedYear.value - 1900,
                month = selectedMonth.value,
                day = selectedDay.value,
                hour = selectedHour.value,
                minutes = selectedMinute.value
            )
        }, hour, minute, true
    )

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier
            .weight(1f)
            .clickable {
                datePickerDialog.show()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        BorderStroke(1.dp, colorScheme.outline),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dateText: String =
                        if (selectedYear.value == 0 && selectedMonth.value == 0 && selectedDay.value == 0) {
                            "Select date"
                        } else {
                            "${selectedDay.value}.${selectedMonth.value + 1}.${selectedYear.value}"
                        }
                    Text(
                        text = dateText,
                        modifier = Modifier.weight(6f),
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.weight(1f))
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier
            .weight(1f)
            .clickable {
                if (selectedYear.value == 0 && selectedMonth.value == 0 && selectedDay.value == 0) {
                    Toast
                        .makeText(context, "Select date first", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    timePickerDialog.show()
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(
                        BorderStroke(1.dp, colorScheme.outline),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val timeText = if (selectedDate == null) {
                        "Select time"
                    } else {
                        selectedDate?.let {
                            val sdf = SimpleDateFormat("HH:mm", Locale.ENGLISH)
                            sdf.format(it)
                        }
                    }
                    Text(
                        text = timeText ?: "no time",
                        modifier = Modifier.weight(6f),
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Outlined.Timer, null, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Event creation page 1 asks the user to select:
 * - A club to post it in
 * - Name and description
 * - Location of event
 * - Date and time
 * - How many members can participate
 *
 * @param vm [CreateEventViewModel]
 */
@Composable
fun EventCreationPage1(vm: CreateEventViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val joinedClubs by vm.joinedClubs.observeAsState(listOf())

    // Event details
    val selectedClub by vm.selectedClub.observeAsState(null)
    val selectedDate by vm.selectedDate.observeAsState(null)
    val eventName by vm.eventName.observeAsState(null)
    val eventDescription by vm.eventDescription.observeAsState(null)
    val eventLocation by vm.eventLocation.observeAsState(null)
    val eventParticipantLimit by vm.eventParticipantLimit.observeAsState(null)

    Column(modifier = Modifier.fillMaxSize()) {
        CreationPageTitle(text = "Create a new event!", modifier = Modifier.padding(bottom = 20.dp))
        ClubSelectionDropdownMenu(joinedClubs, onSelect = {
            vm.updateSelectedClub(it.ref)
        })
        CustomOutlinedTextField(
            value = eventName ?: TextFieldValue(""),
            onValueChange = { vm.updateEventName(it) },
            focusManager = focusManager,
            keyboardType = KeyboardType.Text,
            label = "Name *",
            placeholder = "Give your event a name *",
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
        )
        CustomOutlinedTextField(
            value = eventDescription ?: TextFieldValue(""),
            onValueChange = { vm.updateEventDescription(it) },
            focusManager = focusManager,
            keyboardType = KeyboardType.Text,
            label = "Description *",
            placeholder = "Describe your event *",
            modifier = Modifier
                .height((screenHeight * 0.2).dp)
                .fillMaxWidth()
        )
        CustomOutlinedTextField(
            value = eventLocation ?: TextFieldValue(""),
            onValueChange = { vm.updateEventLocation(it) },
            focusManager = focusManager,
            keyboardType = KeyboardType.Text,
            label = "Location *",
            placeholder = "Give the address of the event *",
            singleLine = true,
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
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp),
        ) {
            PageProgression(
                numberOfLines = 1,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (
                        selectedClub == null ||
                        selectedDate == null ||
                        eventName == null ||
                        eventDescription == null ||
                        eventLocation == null
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
            ) {
                Text(text = "Next")
            }
        }
    }
}

/**
 * Event creation page 2 asks the user to select:
 * - Event banner images
 *
 * @param vm [CreateEventViewModel]
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun EventCreationPage2(vm: CreateEventViewModel) {

    val selectedImages by vm.selectedImages.observeAsState(null)
    var showImagePreview by remember { mutableStateOf(false) }

    // Launcher for selecting images from devices storage.
    // Returns a list of uris for images you have selected.
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            vm.temporarilyStoreImages(uriList.toMutableList())
            showImagePreview = true
        }

    Column(modifier = Modifier.fillMaxSize()) {
        CreationPageTitle(
            text = "Add images about the event",
            modifier = Modifier.padding(bottom = 10.dp)
        )
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { galleryLauncher.launch("image/*") }) {
            Text(text = "Choose images from gallery")
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            CreationPageSubtitle(text = "Saved images", modifier = Modifier.padding(bottom = 20.dp))
            val pagerState = rememberPagerState()
            selectedImages?.let {
                HorizontalPager(
                    count = it.size,
                    state = pagerState,
                    itemSpacing = 10.dp,
                    contentPadding = PaddingValues(end = 150.dp)
                ) { page ->
                    SelectedImageItem(
                        uri = it[page],
                        onDelete = { vm.removeImageFromList(uri = it[page]) })
                }
                if (it.size > 1) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                    )
                }
            } ?: Box(modifier = Modifier.size(150.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Column {
            Column(
                modifier = Modifier
                    .padding(bottom = 32.dp),
            ) {
                PageProgression(
                    numberOfLines = 2,
                    onClick1 = { vm.changePageTo(1) },
                    onClick2 = { vm.changePageTo(2) },
                    onClick3 = { vm.changePageTo(3) },
                    onClick4 = { vm.changePageTo(4) },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Button(modifier = Modifier.weight(1f), onClick = { vm.changePageTo(3) }) {
                        Text(text = "Next")
                    }
                }
            }
        }
    }
}

/**
 * Event creation page 3 asks the user to provide:
 * - Link name
 * - Url for that link
 *
 * @param vm [CreateEventViewModel]
 */
@Composable
fun EventCreationPage3(vm: CreateEventViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    // for changing focus after link added
    var linkSent by remember { mutableStateOf(false) }

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinksLiveData.observeAsState(mapOf())

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
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
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
            }) {
            Text(text = "Add Link")
        }
        CreationPageSubtitle(text = "Provided links", modifier = Modifier.padding(bottom = 20.dp))
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
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp),
        ) {
            PageProgression(
                numberOfLines = 3,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { vm.changePageTo(2) },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = colorScheme.onSurfaceVariant,
                        containerColor = colorScheme.surfaceVariant
                    ),
                ) {
                    Text(text = "Previous")
                }
                Button(modifier = Modifier.weight(1f), onClick = { vm.changePageTo(4) }) {
                    Text(text = "Next")
                }
            }
        }
    }
}

/**
 * Event creation page 4 asks the user to provide:
 * - Contact information
 * - Event Privacy
 *
 * @param vm [CreateEventViewModel]
 * @param navController for Compose navigation
 */
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

    // Second page
    val selectedImages by vm.selectedImages.observeAsState()

    // Third Page
    val linkArray by vm.givenLinksLiveData.observeAsState(null)

    // Last Page
    val currentUser by vm.currentUser.observeAsState()
    val contactInfoName by vm.contactInfoName.observeAsState(null)
    val contactInfoEmail by vm.contactInfoEmail.observeAsState(null)
    val contactInfoNumber by vm.contactInfoNumber.observeAsState(null)
    val eventIsPrivate by vm.eventIsPrivate.observeAsState(false)
    val publicSelected by vm.publicSelected.observeAsState(true)
    val privateSelected by vm.privateSelected.observeAsState(false)

    val scope = rememberCoroutineScope()

    // fetch details for the current user for quick fill options
    if (currentUser == null) {
        LaunchedEffect(Unit) {
            scope.launch {
                vm.getCurrentUser()
            }
        }
    }

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
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { currentUser?.let { vm.quickFillOptions(it) } }) {
            Text(text = "Quick fill")
        }
        SelectPrivacy(
            selectedPublic = publicSelected,
            selectedPrivate = privateSelected,
            onClickPublic = { vm.updateEventPrivacySelection(leftVal = true, rightVal = false) },
            onClickPrivate = { vm.updateEventPrivacySelection(leftVal = false, rightVal = true) }
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp),
        ) {
            PageProgression(
                numberOfLines = 4,
                onClick1 = { vm.changePageTo(1) },
                onClick2 = { vm.changePageTo(2) },
                onClick3 = { vm.changePageTo(3) },
                onClick4 = { vm.changePageTo(4) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { vm.changePageTo(3) },
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
                        if (
                            selectedClub == null ||
                            selectedDate == null ||
                            eventName == null ||
                            eventIsPrivate == null ||
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
                            return@Button
                        } else {
                            val timestamp = Timestamp(selectedDate!!)
                            val listWithYouAsAdmin = mutableListOf<String>()
                            FirebaseHelper.uid?.let {
                                listWithYouAsAdmin.add(it)
                            }

                            val event = Event(
                                clubId = selectedClub!!,
                                name = eventName!!.text,
                                description = eventDescription!!.text,
                                date = timestamp,
                                address = eventLocation!!.text,
                                admins = listWithYouAsAdmin,
                                isPrivate = eventIsPrivate,
                                participantLimit = if (eventParticipantLimit == null || eventParticipantLimit!!.text.isBlank()) -1 else eventParticipantLimit!!.text.toInt(),
                                linkArray = if (linkArray == null || linkArray!!.isEmpty()) mapOf() else linkArray!!,
                                contactInfoName = contactInfoName!!.text,
                                contactInfoEmail = contactInfoEmail!!.text,
                                contactInfoNumber = contactInfoNumber!!.text,
                            )
                            val eventId = vm.addEvent(event)
                            vm.storeImagesOnFirebase(
                                listToStore = selectedImages?.toList() ?: listOf(),
                                eventId = eventId
                            )
                            Toast.makeText(context, "Event created.", Toast.LENGTH_SHORT).show()
                            navController.navigate(NavRoute.Home.name)
                        }
                    },
                ) {
                    Text(text = "Create Event")
                }
            }
        }
    }
}
