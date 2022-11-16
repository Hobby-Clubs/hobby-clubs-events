package com.example.hobbyclubs.screens.event

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hobbyclubs.R
import com.example.hobbyclubs.api.Event
import com.example.hobbyclubs.screens.clubpage.CustomButton

@Composable
fun EventScreen(
    navController: NavController,
    vm: EventScreenViewModel = viewModel(),
    eventId: String
) {
    val context = LocalContext.current
    val event by vm.selectedEvent.observeAsState(null)
    val screenWidth = LocalConfiguration.current.screenWidthDp

    LaunchedEffect(Unit) {
        vm.getEvent(eventId)
    }

    event?.let {
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                EventHeader(navController, context, it, vm)
                Box(
                    modifier = Modifier
                        .width((screenWidth * 0.8).dp)
                        .height(0.5.dp)
                        .background(color = Color.Gray)
                        .align(Alignment.CenterHorizontally)
                )
                EventDescription(it.description)
                Box(
                    modifier = Modifier
                        .width((screenWidth * 0.8).dp)
                        .height(0.5.dp)
                        .background(color = Color.Gray)
                        .align(Alignment.CenterHorizontally)
                )
                EventLocation(it.address)
                Box(
                    modifier = Modifier
                        .width((screenWidth * 0.8).dp)
                        .height(0.5.dp)
                        .background(color = Color.Gray)
                        .align(Alignment.CenterHorizontally)
                )
                // EventLinks()
                // EventContactInfo()
            }
        }
    }
}

@Composable
fun EventHeader(navController: NavController, context: Context, event: Event, vm: EventScreenViewModel) {
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.25).dp)

            ) {
                Image(
                    painter = painterResource(id = R.drawable.hockey),
                    contentDescription = "Event banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight * 0.25).dp),
                    contentScale = ContentScale.FillWidth
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column() {
                        Text(
                            text = "Event name",
                            fontSize = 24.sp,
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "Date and time",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start,
                        )
                    }
                    Column() {
                        Text(
                            text = "Club name",
                            fontSize = 24.sp,
                            textAlign = TextAlign.End,
                        )
                        Text(
                            text = "14 participants",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Start,
                        )
                    }
                }

            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 20.dp, bottom = 20.dp), horizontalArrangement = Arrangement.Start
        ) {
            CustomButton(
                text = "Join",
                onClick = {

                })

        }
    }
}

@Composable
fun EventDescription(desc: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = desc, fontSize = 14.sp)
    }
}

@Composable
fun EventLocation(address: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Location")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = address, fontSize = 14.sp)
    }
}

@Composable
fun EventLinks() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Location")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "links", fontSize = 14.sp)
    }
}

@Composable
fun EventContactInfo() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        EventTitle(text = "Contact Information")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "contact info", fontSize = 14.sp)
    }
}

@Composable
fun EventTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}