package com.example.hobbyclubs.general

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.screens.home.FakeNavigation
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BasicText(value: String) {
    Text(text = value)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTopBar(drawerState: DrawerState, hasSearch: Boolean = false) {
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BurgerMenuButton {
            if (drawerState.isClosed) {
                scope.launch {
                    drawerState.open()
                }
            }
        }
        if (hasSearch) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TopSearchBar(
                    modifier = Modifier
                        .width((screenWidth * 0.72).dp)
                        .aspectRatio(4.64f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSearchBar(modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf("") }
    OutlinedTextField(
        modifier = modifier,
        value = input,
        onValueChange = { input = it },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "search") })
}

@Composable
fun BurgerMenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Filled.Menu,
            contentDescription = "menu"
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(
    navController: NavController,
    drawerState: DrawerState,
    topBar: @Composable (DrawerState) -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = { FakeNavigation(navController = navController) }) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { topBar(drawerState) }
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
            ) {
                content()
            }
        }
    }
}

@Composable
fun LazyColumnHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = text,
            fontWeight = FontWeight.Light,
            fontSize = 24.sp
        )
    }
}

@Composable
fun PicturePicker(modifier: Modifier = Modifier, uri: Uri?, onPick: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { picUri ->
            picUri?.let {
                onPick(it)
            }
        }
    )

    Card(
        modifier = modifier
            .clickable { launcher.launch("image/*") },
        shape = CircleShape,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(),
            model = uri,
            contentDescription = "pic",
            contentScale = ContentScale.Crop
        )
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

@Composable
fun DividerLine(width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(1.dp)
            .background(color = Color.Black)
    )
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