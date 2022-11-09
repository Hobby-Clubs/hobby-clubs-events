package com.example.hobbyclubs.screens.create.event

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.compose.nokiaLighterBlue
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.clubpage.CustomButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

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
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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

@Composable
fun EventCreationPage1(vm: CreateEventViewModel) {
    val focusManager = LocalFocusManager.current
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val eventName by vm.eventName.observeAsState(null)
    val eventDescription by vm.eventDescription.observeAsState(null)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Create a new event!",
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
            )
            CustomOutlinedTextField(
                value = eventDescription ?: TextFieldValue(""),
                onValueChange = { vm.updateEventDescription(it) },
                focusManager = focusManager,
                keyboardType = KeyboardType.Text,
                label = "Description",
                placeholder = "Describe your event",
                modifier = Modifier
                    .height((screenHeight * 0.4).dp)
                    .fillMaxWidth()
            )
        }
        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 75.dp)) {
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

@Composable
fun EventCreationPage2(vm: CreateEventViewModel) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Page 2",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )

        }
        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 75.dp)) {
            PageProgression(numberOfLines = 2, vm)
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(1) },
                    text = "Previous",
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

@Composable
fun EventCreationPage3(vm: CreateEventViewModel) {

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Page 3",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 75.dp)) {
            PageProgression(numberOfLines = 3, vm)
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(2) },
                    text = "Previous",
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
    val eventName by vm.eventName.observeAsState(null)
    val eventDescription by vm.eventDescription.observeAsState(null)

    // Last Page
    val contactInfoName by vm.contactInfoName.observeAsState(null)
    val contactInfoEmail by vm.contactInfoEmail.observeAsState(null)
    val contactInfoNumber by vm.contactInfoNumber.observeAsState(null)


    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
        }
        Column(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 75.dp)) {
            PageProgression(numberOfLines = 4, vm)
            Row() {
                CustomButton(
                    onClick = { vm.changePageTo(3) },
                    text = "Previous",
                    modifier = Modifier
                        .height(60.dp)
                )
                CustomButton(
                    onClick = {
                        if (eventName == null || eventDescription == null || contactInfoName == null || contactInfoEmail == null || contactInfoNumber == null) {
                            Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                            return@CustomButton
                        } else {
                            val event = Event(
                                clubId = "9H7A1soQdHcQ0U6a0AfO",
                                name = eventName!!.text,
                                description = eventDescription!!.text,
                                date = Timestamp.now(),
                                address = "Karaportti 2",
                                participantLimit = 5,
                                contactInfoName = contactInfoName!!.text,
                                contactInfoEmail = contactInfoEmail!!.text,
                                contactInfoNumber = contactInfoNumber!!.text
                            )
                            vm.addEvent(event)
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
