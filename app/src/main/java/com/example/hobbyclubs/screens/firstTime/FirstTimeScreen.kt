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
import com.example.hobbyclubs.api.ClubCategory
import com.example.hobbyclubs.api.FirebaseHelper
import com.example.hobbyclubs.navigation.NavRoute

/**
 * First time screen
 *
 * This screen asks the user for his interests. These interests
 * will later be used for suggesting clubs to the user.
 *
 * @param navController
 */

@Composable
fun FirstTimeScreen(navController: NavController) {
    val interests: List<Interest> = listOf(
        Interest(ClubCategory.BoardGames.name, remember { mutableStateOf(false) }),
        Interest(ClubCategory.VideoGames.name, remember { mutableStateOf(false) }),
        Interest(ClubCategory.Music.name, remember { mutableStateOf(false) }),
        Interest(ClubCategory.Movies.name, remember { mutableStateOf(false) }),
        Interest(ClubCategory.Sports.name, remember { mutableStateOf(false) }),
        Interest(ClubCategory.Other.name, remember { mutableStateOf(false) }),
    )

    //on confirm the selection will be passed into the database
    fun onConfirm() {
        val selection = interests.filter { it.interested.value }.map { it.name }
        FirebaseHelper.uid?.let {
            val changeMap = mapOf(
                Pair("interests", selection), Pair("firstTime", false)
            )
            FirebaseHelper.updateUser(it, changeMap)
        }
        navController.navigate(NavRoute.Home.name)
    }

    Surface {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp)
                .padding(top = 40.dp, bottom = 80.dp),
        ) {
            Column {
                Text(
                    text = "HOBBY CLUBS",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 20.dp),
                )
                Divider()
                Text(
                    text = "In order to get club suggestions,\n" + "please select your interests:",
                    modifier = Modifier.padding(top = 20.dp)
                )
                Column(
                    modifier = Modifier.padding(top = 40.dp)
                ) {
                    interests.forEach { interest ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth()
                                .clickable {
                                    interest.interested.value = !interest.interested.value
                                }) {
                            Checkbox(
                                checked = interest.interested.value,
                                onCheckedChange = { interest.interested.value = it },
                            )
                            Text(text = interest.name.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }
            Button(
                onClick = {
                    onConfirm()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Confirm")
            }
        }
    }
}

/**
 * Interest
 *
 * Interests are different kinds of categories and store
 * if the user is interested or not.
 *
 * @property name
 * @property interested
 */

class Interest(val name: String, val interested: MutableState<Boolean>)