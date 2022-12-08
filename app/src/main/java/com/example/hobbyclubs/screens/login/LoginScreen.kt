package com.example.hobbyclubs.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.api.User
import com.example.hobbyclubs.general.PicturePicker
import com.example.hobbyclubs.general.SwitchPill
import com.example.hobbyclubs.navigation.NavRoute
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
            navController.navigate(NavRoute.Home.name)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padVal)
                .padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SwitchPill(
                modifier = Modifier
                    .padding(top = 16.dp),
                isFirstSelected = !showRegister,
                onChange = { vm.updateShowRegister(!it) },
                firstText = "Log in",
                secondText = "Register"
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (showRegister) {
                    ProfileForm(vm = vm)
                }
                EmailPwdForm(vm = vm)
            }
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

        if (showRegister) {
            if (vm.fName.value.isNullOrBlank() || vm.lName.value.isNullOrBlank()) {
                vm.updateAuthException(Exception("Please enter a first and last name"))
                return
            }
        }
        if (email.isEmpty() || pwd.isEmpty()) {
            vm.updateAuthException(Exception("Please enter a valid email and password"))
            return
        }
        when (showRegister) {
            true -> {
                val newUser = User(
                    fName = vm.fName.value ?: "",
                    lName = vm.lName.value ?: "",
                    phone = vm.phone.value ?: "",
                    email = email
                )
                vm.register(newUser, pwd)
            }
            false -> vm.login(email, pwd)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { vm.updateEmail(it) },
            label = { Text(text = "Email") },
            singleLine = true
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = pwd,
            onValueChange = { vm.updatePwd(it) },
            label = { Text(text = "Password") },
            singleLine = true
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileForm(vm: LoginViewModel) {
    val picUri by vm.picUri.observeAsState()
    val fName by vm.fName.observeAsState("")
    val lName by vm.lName.observeAsState("")
    val phone by vm.phone.observeAsState("")

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        PicturePicker(modifier = Modifier.size(170.dp), uri = picUri, onPick = { vm.updateUri(it) })
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fName,
            onValueChange = { vm.updateFName(it) },
            label = { Text(text = "First name") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lName,
            onValueChange = { vm.updateLName(it) },
            label = { Text(text = "Last name") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = phone,
            onValueChange = { vm.updatePhone(it) },
            label = { Text(text = "Phone number (optional)") }
        )
    }
}