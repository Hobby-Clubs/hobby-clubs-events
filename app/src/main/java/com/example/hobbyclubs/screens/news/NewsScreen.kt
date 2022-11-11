package com.example.hobbyclubs.screens.news

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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.navigation.NavRoutes
import com.example.hobbyclubs.screens.home.FakeButtonForNavigationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(navController: NavController,vm: NewsViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val allNews = vm.listOfNews.observeAsState(listOf())
    LaunchedEffect(Unit){
        scope.launch(Dispatchers.IO) { vm.getALlNews() }

    }
    Box() {

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
                FakeButtonForNavigationTest(destination ="Create News" ) {
                    navController.navigate(NavRoutes.CreateNewsScreen.route)
                }


            Spacer(modifier = Modifier.padding(top = 10.dp))

           if (allNews.value.isNotEmpty()) {
               Dashboard(newsList = allNews.value)
           }

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
fun Dashboard(newsList: List<News>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        newsList.forEach {
            item { ImageCard(it)}
        }
    }
}

@Composable
fun ImageCard(news: News) {
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
                painter = painterResource(id = R.drawable.ice),
                contentDescription = "image.text",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp)
                    .clip(RoundedCornerShape(16.dp))

            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = news.headline,  fontWeight = FontWeight.Bold)
//                Text(text = news.date.toString())
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
                Text(text =news.newsContent)
            } else {
                Text(text =news.newsContent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )}
        }
    }
}
