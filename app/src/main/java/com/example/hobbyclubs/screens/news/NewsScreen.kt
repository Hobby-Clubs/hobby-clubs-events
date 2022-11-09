package com.example.hobbyclubs.screens.news

import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.hobbyclubs.general.BasicText
import com.example.hobbyclubs.general.SwitchPill

@Composable
fun NewsScreen(navController: NavController) {
    BasicText(value = "News Screen")
    var showMyNews by remember{ mutableStateOf(true) }
    SwitchPill(isFirstSelected = showMyNews, onChange = {isFirst -> showMyNews = isFirst}, firstText ="My News", secondText ="All News" )
}