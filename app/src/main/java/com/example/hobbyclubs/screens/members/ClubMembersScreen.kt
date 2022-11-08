package com.example.hobbyclubs.screens.members

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.compose.forestGreen
import com.example.hobbyclubs.R
import com.example.hobbyclubs.screens.clubpage.CustomButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubMembersScreen(navController: NavController, showRequests: Boolean) {
    val listOfMembers = listOf("Matti Meikäläinen", "Matin Veli", "Matin Isä")

    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = if (showRequests) "Member Requests" else "Members",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 75.dp, bottom = 20.dp),
            )
            ListOfMembers(listOfMembers, showRequests)
        }
        CenterAlignedTopAppBar(
            title = { Text(text = "Ice Hockey Club", fontSize = 16.sp) },
            colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }
}

@Composable
fun ListOfMembers(listOfMembers: List<String>, showRequests: Boolean) {
    var selectedMemberIndex: Int? by remember { mutableStateOf(null) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(listOfMembers) { index, member ->
            MemberCard(
                name = member,
                showRequests = showRequests,
                setSelectedMemberIndex = { selectedMemberIndex = index },
                isSelected = selectedMemberIndex == index
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberCard(
    name: String,
    showRequests: Boolean,
    setSelectedMemberIndex: () -> Unit,
    isSelected: Boolean
) {
    val context = LocalContext.current
    var expandedState by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        onClick = {
            if (!showRequests) {
                setSelectedMemberIndex()
                expandedState = true
            }
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemberImage(avatarId = R.drawable.humanface)
                Text(
                    text = name, fontSize = 16.sp, modifier = Modifier
                        .weight(6f)
                        .padding(start = 30.dp)
                )
                if (showRequests) {
                    Icon(
                        Icons.Outlined.Cancel,
                        "Decline",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                Toast
                                    .makeText(context, "Declined", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        Icons.Outlined.TaskAlt,
                        "Accept",
                        tint = forestGreen,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                Toast
                                    .makeText(context, "Accepted", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    )
                }
            }
            if (isSelected && expandedState) {
                Spacer(modifier = Modifier.height(5.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CustomButton(
                        onClick = {
                            Toast
                                .makeText(context, "Promoted to admin", Toast.LENGTH_SHORT)
                                .show()
                        },
                        text = "Promote to admin",
                    )
                    CustomButton(
                        onClick = {
                            Toast
                                .makeText(context, "Kicked", Toast.LENGTH_SHORT)
                                .show()
                        },
                        text = "Kick",
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    )
                }
            }

        }
    }
}

@Composable
fun MemberImage(avatarId: Int) {
    Card(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.size(50.dp)
    ) {
        Image(
            painter = painterResource(avatarId),
            contentDescription = "avatar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }
}
