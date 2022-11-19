package com.example.hobbyclubs.screens.firstTime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hobbyclubs.navigation.NavRoutes

@Composable
fun FirstTimeScreen(navController: NavController) {

    Surface {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .padding(top = 40.dp, bottom = 80.dp),
        ) {
            val interests: List<Interest> =
                listOf(
                    Interest("Sports", remember { mutableStateOf(false) }),
                    Interest("Games", remember { mutableStateOf(false) }),
                    Interest("Mechanics", remember { mutableStateOf(false) }),
                    Interest("Computers", remember { mutableStateOf(false) }),
                    Interest("Chess", remember { mutableStateOf(false) }),
                )

            Column {
                Text(
                    text = "HOBBY CLUBS",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
                Divider()
                Text(
                    text = "In order to get club suggestions,\n" +
                            "please select your interests:",
                    modifier = Modifier.padding(top = 20.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(top = 40.dp)
                ) {
                    interests.forEach { interest ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                                .clickable {
                                    interest.interested.value = !interest.interested.value
                                }
                        ) {
                            Checkbox(
                                checked = interest.interested.value,
                                onCheckedChange = { interest.interested.value = it },
                            )
                            Text(text = interest.name)
                        }
                    }
                }
            }
            Button(
                onClick = { navController.navigate(NavRoutes.HomeScreen.route) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Confirm")
            }
        }
    }
}

class Interest(val name: String, val interested: MutableState<Boolean>)