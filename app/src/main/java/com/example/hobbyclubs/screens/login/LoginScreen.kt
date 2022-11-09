package com.example.hobbyclubs.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.navigation.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, vm: LoginViewModel = viewModel()) {
    val isLoggedIn by vm.isLoggedIn.observeAsState(false)
    val authException by vm.authException.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val showRegister by vm.showRegister.observeAsState(false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate(NavRoutes.HomeScreen.route)
        }
    }

    LaunchedEffect(authException) {
        authException?.let { e ->
            e.message?.let { msg ->
                scope.launch(Dispatchers.Main) {
                    snackbarHostState.showSnackbar(
                        message = msg,
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = true
                    )
                }
            }
            return@LaunchedEffect
        }
        scope.launch(Dispatchers.Main) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padVal ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padVal)
                .padding(horizontal = 32.dp), contentAlignment = Alignment.Center
        ) {
            SwitchPill(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                isFirstSelected = !showRegister,
                onChange = { vm.updateShowRegister(!it) },
                firstText = "Log in",
                secondText = "Register"
            )
            EmailPwdForm(vm = vm)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EmailPwdForm(vm: LoginViewModel) {
    val kbController = LocalSoftwareKeyboardController.current
    val email by vm.email.observeAsState("")
    val pwd by vm.pwd.observeAsState("")
    val showRegister by vm.showRegister.observeAsState(false)

    fun handleAuth() {
        vm.updateAuthException(null)
        if (email.isNotEmpty() && pwd.isNotEmpty()) {
            when (showRegister) {
                true -> vm.register(email, pwd)
                false -> vm.login(email, pwd)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { vm.updateEmail(it) },
            label = { Text(text = "Email") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pwd,
            onValueChange = { vm.updatePwd(it) },
            label = { Text(text = "Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            kbController?.hide()
            handleAuth()
        }) {
            Text(text = if (showRegister) "Register" else "Log in")
        }
    }
}

@Composable
fun SwitchPill(
    modifier: Modifier = Modifier,
    isFirstSelected: Boolean,
    onChange: (Boolean) -> Unit,
    firstText: String,
    secondText: String
) {
    Row(modifier = modifier) {
        Pill(modifier = Modifier.weight(1f), isSelected = isFirstSelected, text = firstText) {
            onChange(true)
        }
        Pill(
            modifier = Modifier.weight(1f),
            isLeft = false,
            isSelected = !isFirstSelected,
            text = secondText
        ) {
            onChange(false)
        }
    }
}

@Composable
fun Pill(
    modifier: Modifier = Modifier,
    isLeft: Boolean = true,
    isSelected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
    } else {
        RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
    }

    val color =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    Card(
        shape = shape,
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(color)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Text(modifier = Modifier.padding(8.dp), text = text)
        }
    }
}