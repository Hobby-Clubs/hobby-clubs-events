package com.example.hobbyclubs.screens.eventmanagement

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.general.*
import com.example.hobbyclubs.navigation.NavRoute
import com.example.hobbyclubs.screens.clubmanagement.EmptySurface
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Event management screen for displayed management options of an event for admin users
 *
 * @param navController To manage app navigation within the NavHost
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EventManagementScreen(
    navController: NavController,
    vm: EventManagementViewModel = viewModel(),
    eventId: String
) {
    val event by vm.selectedEvent.observeAsState(null)
    val listOfRequests by vm.listOfRequests.observeAsState(listOf())
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        confirmStateChange = { it != ModalBottomSheetValue.HalfExpanded }
    )
    val scope = rememberCoroutineScope()
    var sheetContent: @Composable () -> Unit by remember { mutableStateOf({ EmptySurface() }) }
    var showDeleteEventDialog by remember { mutableStateOf(false) }

    BackHandler(sheetState.isVisible) {
        scope.launch { sheetState.hide() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { sheetContent() },
        modifier = Modifier.fillMaxSize(),
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        LaunchedEffect(Unit) {
            vm.getEvent(eventId)
            vm.getAllJoinRequests(eventId)
        }
        LaunchedEffect(event) {
            event?.let {
                vm.fillPreviousEventData(it)
            }
        }

        event?.let { event ->
            Scaffold() {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = it.calculateBottomPadding()),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Manage event",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
                    )
                    ParticipantsSection(
                        navController = navController,
                        eventId = eventId,
                        participantAmount = event.participants.size,
                        requestAmount = listOfRequests.size
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    EventManagementSectionTitle(text = "Edit event details")
                    EventManagementRowItem(text = "Name and description") {
                        sheetContent = {
                            NameAndDescriptionSheet(vm = vm, eventId = eventId) {
                                scope.launch { if (sheetState.isVisible) sheetState.hide() }
                            }
                        }
                        scope.launch {
                            if (sheetState.isVisible) sheetState.hide()
                            else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EventManagementRowItem(text = "Location, date and participants") {
                        sheetContent = {
                            LocationDateParticipantsSheet(vm = vm, eventId = eventId) {
                                scope.launch { if (sheetState.isVisible) sheetState.hide() }
                            }
                        }
                        scope.launch {
                            if (sheetState.isVisible) sheetState.hide()
                            else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EventManagementRowItem(text = "Event images") {
                        sheetContent = {
                            ImagesSheet(vm = vm, eventId = eventId) {
                                scope.launch { if (sheetState.isVisible) sheetState.hide() }
                            }
                        }
                        scope.launch {
                            if (sheetState.isVisible) sheetState.hide()
                            else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EventManagementRowItem(text = "Socials") {
                        sheetContent = {
                            SocialsSheet(vm = vm, eventId = eventId) {
                                scope.launch { if (sheetState.isVisible) sheetState.hide() }
                            }
                        }
                        scope.launch {
                            if (sheetState.isVisible) sheetState.hide()
                            else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EventManagementRowItem(text = "Contact information") {
                        sheetContent = {
                            ContactSheet(vm = vm, eventId = eventId) {
                                scope.launch { if (sheetState.isVisible) sheetState.hide() }
                            }
                        }
                        scope.launch {
                            if (sheetState.isVisible) sheetState.hide()
                            else sheetState.animateTo(ModalBottomSheetValue.Expanded)
                        }
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        onClick = {
                            showDeleteEventDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError,
                        ),
                    ) {
                        Icon(Icons.Outlined.Delete, null)
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = "Delete Event",
                        )
                    }
                }
                if (showDeleteEventDialog) {
                    CustomAlertDialog(
                        onDismissRequest = { showDeleteEventDialog = false },
                        onConfirm = {
                            scope.launch {
                                vm.deleteEvent(eventId)
                                navController.navigate(NavRoute.Home.name)
                            }
                        },
                        title = "Delete event",
                        text = "Are you sure you want to delete this event? There is no going back.",
                        confirmText = "Delete"
                    )

                }
                CenterAlignedTopAppBar(
                    title = { Text(text = event.name, fontSize = 16.sp) },
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
 * Title within the event management section
 *
 * @param text String that will be displayed as the title
 */
@Composable
fun EventManagementSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 15.dp)
    )
}

/**
 * Row item within the event management section
 *
 * @param text String that will be displayed in the item
 * @param onClick Action for when the item is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventManagementRowItem(text: String, onClick: () -> Unit) {
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
 * Participants within the event management section
 *
 * @param navController To manage app navigation within the NavHost
 * @param eventId UID for the specific event
 * @param participantAmount Amount of participants
 * @param requestAmount Amount of join requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsSection(
    navController: NavController,
    eventId: String,
    participantAmount: Int,
    requestAmount: Int
) {
    Column {
        EventManagementSectionTitle(text = "Participants")
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = { navController.navigate(NavRoute.EventParticipants.name + "/$eventId") },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.People, "People Icon", modifier = Modifier
                        .size(30.dp)
                )
                Text(
                    text = "Participants", fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
                EventManagementRowNumberCount(
                    modifier = Modifier
                        .size(24.dp),
                    numberOfItem = participantAmount
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            onClick = { navController.navigate(NavRoute.EventParticipantRequest.name + "/$eventId") },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.PersonAddAlt, "Person add Icon", modifier = Modifier
                        .size(30.dp)
                )
                Text(
                    text = "Participant Requests", fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
                EventManagementRowNumberCount(
                    modifier = Modifier
                        .size(24.dp),
                    numberOfItem = requestAmount,
                    isParticipantRequestSection = true
                )
            }
        }
    }
}

/**
 * Container for the number displaying the amount of participants or join requests
 *
 * @param modifier Collection of modifier elements that decorate or add behavior to the UI element
 * @param numberOfItem Number of participants/requests
 * @param isParticipantRequestSection Boolean value of whether or not the number is related to join requests
 */
@Composable
fun EventManagementRowNumberCount(
    modifier: Modifier,
    numberOfItem: Int,
    isParticipantRequestSection: Boolean = false
) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = if (isParticipantRequestSection && numberOfItem > 0) MaterialTheme.colorScheme.error else Color.Transparent),
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = numberOfItem.toString(),
                fontSize = 16.sp,
                color = if (isParticipantRequestSection && numberOfItem > 0) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

/**
 * Sheet for editing the event's name and description
 *
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 * @param onSave Action for when the save button is clicked
 */
@Composable
fun NameAndDescriptionSheet(vm: EventManagementViewModel, eventId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val eventName by vm.eventName.observeAsState(TextFieldValue(""))
    val eventDescription by vm.eventDescription.observeAsState(TextFieldValue(""))

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
            Column() {
                Text(
                    text = "Edit Name and Description",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                CustomOutlinedTextField(
                    value = eventName ?: TextFieldValue(""),
                    onValueChange = { vm.updateEventName(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Name",
                    placeholder = "Give your event a name",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )

                CustomOutlinedTextField(
                    value = eventDescription ?: TextFieldValue(""),
                    onValueChange = { vm.updateEventDescription(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Description",
                    placeholder = "Describe your event",
                    modifier = Modifier
                        .height((screenHeight * 0.3).dp)
                        .fillMaxWidth()
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    if (eventName.text.isNotEmpty() && eventDescription.text.isNotBlank()) {
                        val changeMap = mapOf(
                            Pair("name", eventName!!.text),
                            Pair("description", eventDescription!!.text),
                        )
                        vm.updateEventDetails(eventId = eventId, changeMap)
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
 * Sheet for editing the event's location, date and participant limit
 *
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 * @param onSave Action for when the save button is clicked
 */
@Composable
fun LocationDateParticipantsSheet(
    vm: EventManagementViewModel,
    eventId: String,
    onSave: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val eventLocation by vm.eventAddress.observeAsState(TextFieldValue(""))
    val eventParticipantLimit by vm.eventParticipantLimit.observeAsState(TextFieldValue(""))
    val eventDate by vm.eventDate.observeAsState(Date())

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
            Column(
            ) {
                Text(
                    text = "Edit location",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                CustomOutlinedTextField(
                    value = eventLocation ?: TextFieldValue(""),
                    onValueChange = { vm.updateEventAddress(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Address",
                    placeholder = "Give the address of the event",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = "Edit participants",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )
                CustomOutlinedTextField(
                    value = eventParticipantLimit ?: TextFieldValue(""),
                    onValueChange = { vm.updateEventParticipantLimit(it) },
                    focusManager = focusManager,
                    keyboardType = KeyboardType.Text,
                    label = "Participant limit",
                    placeholder = "Leave empty for no limit",
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text(
                    text = "Edit date and time",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
                )
                DateSelector(vm = vm)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (eventLocation.text.isNotEmpty() && eventParticipantLimit.text.isNotBlank() && eventDate != null) {
                        val timestamp = Timestamp(eventDate)
                        val changeMap = mapOf(
                            Pair("address", eventLocation!!.text),
                            Pair("participantLimit", eventParticipantLimit!!.text.toInt()),
                            Pair("date", timestamp)
                        )
                        vm.updateEventDetails(eventId = eventId, changeMap)
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
 * Selector for date of the event
 *
 * @param vm [EventManagementViewModel]
 */
@Composable
fun DateSelector(vm: EventManagementViewModel) {
    val context = LocalContext.current
    val selectedDate by vm.eventDate.observeAsState()
    val calendar = Calendar.getInstance()

    if (selectedDate != null) calendar.time = selectedDate!!
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val selectedYear = remember { mutableStateOf(year) }
    val selectedMonth = remember { mutableStateOf(month) }
    val selectedDay = remember { mutableStateOf(day) }
    val selectedHour = remember { mutableStateOf(hour) }
    val selectedMinute = remember { mutableStateOf(minute) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, mYear: Int, mMonth: Int, dayOfMonth: Int ->
            selectedYear.value = mYear
            selectedMonth.value = mMonth
            selectedDay.value = dayOfMonth
            vm.updateEventDate(
                years = selectedYear.value - 1900,
                month = selectedMonth.value,
                day = selectedDay.value,
                hour = selectedHour.value,
                minutes = selectedMinute.value
            )
        },
        year, month, day,
    )

    val timePickerDialog = TimePickerDialog(
        context,
        3,
        { _, mHour: Int, mMinute: Int ->
            selectedHour.value = mHour
            selectedMinute.value = mMinute
            vm.updateEventDate(
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
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val dateText: String =
                        if (selectedYear.value == 0 && selectedMonth.value == 0 && selectedDay.value == 0) {
                            "Select date"
                        } else {
                            "${selectedDay.value}.${selectedMonth.value}.${selectedYear.value}"
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
                if (selectedYear.value == 0 || selectedMonth.value == 0 || selectedDay.value == 0) {
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
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
 * Sheet for managing the event's social links
 *
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 * @param onSave Action for when the save button is clicked
 */
@Composable
fun SocialsSheet(vm: EventManagementViewModel, eventId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var linkSent by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val currentLinkName by vm.currentLinkName.observeAsState(null)
    val currentLinkURL by vm.currentLinkURL.observeAsState(null)
    val givenLinks by vm.givenLinks.observeAsState(mapOf())

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
            Column() {
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
                        vm.updateEventDetails(eventId = eventId, changeMap)
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
 * Sheet for editing the event's images
 *
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 * @param onSave Action for when the save button is clicked
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesSheet(vm: EventManagementViewModel, eventId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val selectedImages by vm.selectedBannerImages.observeAsState(null)
    var showImagesPreview by remember { mutableStateOf(false) }
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
            vm.temporarilyStoreImages(bannerUri = uriList.toMutableList())
            showImagesPreview = true
        }
    val context = LocalContext.current

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
                    Text(
                        text = "Add images about the event",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { galleryLauncher.launch("image/*") },
                    ) {
                        Text(text = "Choose images from gallery")
                    }
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
                        selectedImages?.let {
                            HorizontalPager(
                                count = it.size,
                                state = pagerState,
                                itemSpacing = 10.dp,
                                contentPadding = PaddingValues(end = 200.dp)
                            ) { page ->
                                SelectedImageItem(
                                    uri = it[page],
                                    onDelete = {
                                        vm.removeImageFromList(it[page])
                                    }
                                )
                            }
                            if (it.size > 1) {
                                HorizontalPagerIndicator(
                                    pagerState = pagerState,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp),
                                )
                            }
                        } ?: Box(modifier = Modifier.size(110.dp))
                    }
                }
            }
            Button(
                onClick = {
                    selectedImages?.let {
                        vm.replaceEventImages(
                            eventId = eventId,
                            newImages = it,
                        )
                        onSave()
                    } ?: Toast
                        .makeText(context, "Select images first", Toast.LENGTH_SHORT)
                        .show()
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
 * Sheet for editing the event's contact information
 *
 * @param vm [EventManagementViewModel]
 * @param eventId UID for the specific event
 * @param onSave Action for when the save button is clicked
 */
@Composable
fun ContactSheet(vm: EventManagementViewModel, eventId: String, onSave: () -> Unit) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val focusManager = LocalFocusManager.current
    val contactInfoName by vm.eventContactName.observeAsState(TextFieldValue(""))
    val contactInfoEmail by vm.eventContactEmail.observeAsState(TextFieldValue(""))
    val contactInfoNumber by vm.eventContactNumber.observeAsState(TextFieldValue(""))

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
            Column(
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
                    onValueChange = { vm.updateContactName(it) },
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
                    onValueChange = { vm.updateContactEmail(it) },
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
                    onValueChange = { vm.updateContactNumber(it) },
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
                        vm.updateEventDetails(eventId, changeMap)
                        onSave()
                    }
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}