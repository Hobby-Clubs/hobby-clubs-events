package com.example.hobbyclubs.screens.news

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.hobbyclubs.api.Club
import com.example.hobbyclubs.api.News
import com.example.hobbyclubs.navigation.NavRoutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    navController: NavController,
   vm: NewsViewModel = viewModel(),

) {
    val scope = rememberCoroutineScope()
    val allNews = vm.listOfNews.observeAsState(listOf())
    LaunchedEffect(Unit){
        scope.launch(Dispatchers.IO) {
            vm.getALlNews()
        }
    }
    Box {

        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)) {
            CenterAlignedTopAppBar(
                title = { },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )

//            Spacer(modifier = Modifier.padding(top = 10.dp))

           if (allNews.value.isNotEmpty()) {
               Dashboard(newsList = allNews.value, navController)
           }

        }
    }
}
@Composable
fun Dashboard(newsList: List<News>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(1.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        newsList.forEach {
            item {
                ImageCard(it, vm = NewsViewModel()
                ) { navController.navigate(NavRoutes.SingleNewsScreen.route + "/${it.id}") }
            }
        }
    }
}

@Composable
fun ImageCard(news: News,
              vm: NewsViewModel,
              onClick: () -> Unit) {

    var newsUri: Uri? by rememberSaveable { mutableStateOf(null) }
    var club: Club? by rememberSaveable { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        if (newsUri == null) {
            vm.getImage(news.id)
                .downloadUrl
                .addOnSuccessListener {
                    newsUri = it
                }
                .addOnFailureListener {
                    Log.e("getImage", "NewsHeadline: ", it)
                }
        }

    if(club == null){
        vm.getClub(news.clubId).get()
            .addOnSuccessListener { data ->
                val fetchedClub = data.toObject(Club::class.java)
                fetchedClub?.let { club = it }
            }
            .addOnFailureListener {
                Log.e("FetchClub", "getClubFail: ", it)
            }
    }
    }

    club?.let {
        println("club: ${it.name}")
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable { onClick() },
        ) {
            Column(modifier = Modifier
                //.padding(10.dp)
                .fillMaxSize(),
            ) {
                AsyncImage(
                    model= newsUri,
                    contentDescription = "news image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                        .clip(RoundedCornerShape(16.dp))

                )
                Column( modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                ) {
                    Row( modifier = Modifier
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = news.headline,  fontWeight = FontWeight.Bold)
                        Text(text = it.name,  fontWeight = FontWeight.Bold)
                    }
                    Row( modifier = Modifier
                        .fillMaxWidth()) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)
                        val dateFormatted = sdf.format(news.date.toDate())
                        Text(text = dateFormatted.toString())
                    }
                  Row( modifier = Modifier
                      .fillMaxWidth().padding(vertical = 5.dp)) {
                      Text(text =news.newsContent,
                          maxLines = 2,
                          overflow = TextOverflow.Ellipsis
                      )}
                  }

            }
        }
    }

}

/*

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
}*/
