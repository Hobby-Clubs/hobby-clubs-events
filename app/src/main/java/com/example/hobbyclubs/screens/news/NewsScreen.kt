package com.example.hobbyclubs.screens.news

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hobbyclubs.R
import com.example.hobbyclubs.general.SwitchPill
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.createnews.CreateNewsViewModel
import com.example.hobbyclubs.screens.home.FakeButtonForNavigationTest

@Composable
fun NewsScreen(navController: NavController,vm: CreateNewsViewModel = viewModel()) {
    Box(modifier = Modifier.padding(10.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {


            var showMyNews by remember { mutableStateOf(true) }
            SwitchPill(
                isFirstSelected = showMyNews,
                onChange = { isFirst -> showMyNews = isFirst },
                firstText = "My News",
                secondText = "All News"
            )
            Row() {
                FakeButtonForNavigationTest(destination ="Create News" ) {
                    navController.navigate(NavRoutes.CreateNewsScreen.route)
                }

            }

            Spacer(modifier = Modifier.padding(top = 10.dp))
            Dashboard(images = images)

        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(state: MutableState<TextFieldValue>) {
    TextField(
        value = state.value,
        onValueChange = { value ->
            state.value = value
        },
        modifier = Modifier
            .fillMaxWidth(),
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp),
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = "",
                modifier = Modifier
                    .padding(15.dp)
                    .size(24.dp)
            )
        },
        trailingIcon = {
            if (state.value != TextFieldValue("")) {
                IconButton(
                    onClick = {
                        state.value =
                            TextFieldValue("") // Remove text from TextField when you press the 'X' icon
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)
                    )
                }
            }
        },
        singleLine = true,
        shape = RectangleShape, // The TextFiled has rounded corners top left and right by default
        colors = TextFieldDefaults.textFieldColors(
            textColor = MaterialTheme.colorScheme.onPrimary,
            cursorColor = MaterialTheme.colorScheme.onPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
@Composable
fun Dashboard(images: List<ImageData>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        images.forEach {
            item { ImageCard(image = it)}
        }
    }
}

@Composable
fun ImageCard(image: ImageData) {
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .clickable { expandedState = !expandedState },
    ) {
        Column(modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = image.drawable),
                contentDescription = image.text,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .clip(RoundedCornerShape(16.dp))

            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = image.text,  fontWeight = FontWeight.Bold)
                IconButton(
                    modifier = Modifier
                        .weight(1f)
                        .rotate(rotationState),
                    onClick = {
                        expandedState = !expandedState
                    }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Drop-Down Arrow"
                    )
                }
            }
            if (expandedState) {
                Text(text =
                "Lorem ipsum dolor sit amet," +
                        " consectetur adipiscing elit." +
                        " Quisque in felis condimentum," +
                        " ultricies erat at, vestibulum leo." +
                        " Donec ipsum turpis, eleifend id libero ut," +
                        " vulputate pulvinar felis. Aliquam sodales mattis eros," +
                        " in maximus lacus mattis nec." +
                        " Nam sed risus sed elit pretium efficitur at eu lorem." +
                        " Proin vitae magna magna. Interdum et malesuada fames ac ante ipsum primis in faucibus. " +
                        "Donec tincidunt pulvinar nisi. Proin lobortis justo in lectus pharetra, " +
                        "eget commodo diam tempor. Morbi tortor turpis, sodales varius ipsum sit amet," +
                        " congue lobortis sem. Nunc facilisis lacus et nisi tempor, at aliquet sem cursus. " +
                        "Suspendisse faucibus felis quis ex tristique, non tempor lacus pretium.")
            } else {
                Text(text =
        "Lorem ipsum dolor sit amet," +
                " consectetur adipiscing elit." +
                " Quisque in felis condimentum," +
                " ultricies erat at, vestibulum leo." +
                " Donec ipsum turpis, eleifend id libero ut," +
                " vulputate pulvinar felis. Aliquam sodales mattis eros," +
                " in maximus lacus mattis nec." +
                " Nam sed risus sed elit pretium efficitur at eu lorem." +
                " Proin vitae magna magna. Interdum et malesuada fames ac ante ipsum primis in faucibus. " +
                "Donec tincidunt pulvinar nisi. Proin lobortis justo in lectus pharetra, " +
                "eget commodo diam tempor. Morbi tortor turpis, sodales varius ipsum sit amet," +
                " congue lobortis sem. Nunc facilisis lacus et nisi tempor, at aliquet sem cursus. " +
                "Suspendisse faucibus felis quis ex tristique, non tempor lacus pretium.",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )}
        }
    }
}

val images = listOf(
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice),
    ImageData("Ice hockey club", R.drawable.ice)
)

data class ImageData(
    val text: String,
    @DrawableRes val drawable: Int
)