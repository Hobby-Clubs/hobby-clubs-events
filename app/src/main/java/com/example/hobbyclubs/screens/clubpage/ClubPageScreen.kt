package com.example.hobbyclubs.screens.clubpage

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.compose.linkBlue
import com.example.compose.nokiaBlue
import com.example.compose.nokiaDarkBlue
import com.example.hobbyclubs.R
import com.example.hobbyclubs.general.DividerLine
import com.example.hobbyclubs.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubPageScreen(navController: NavController) {
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val fakeDesc =
        "We are a professional ice hockey club located in Espoo. We allow everyone to join our club. Lets have fun!"
    val listOfLinks = listOf("Facebook", "Discord", "Twitter")
    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            ClubPageHeader(navController, context)
            DividerLine(width = (screenWidth * 0.9).dp)
            ClubDescription(description = fakeDesc)
            DividerLine(width = (screenWidth * 0.9).dp)
            ClubSchedule()
            DividerLine(width = (screenWidth * 0.9).dp)
            ClubLinks(listOfLinks, context)
            DividerLine(width = (screenWidth * 0.9).dp)
            ClubContactInfo(
                name = "Matti Meikäläinen",
                phoneNumber = "+358 501234567",
                email = "matti@email.fi"
            )
        }
        TopAppBar(
            title = {},
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
fun ClubPageHeader(navController: NavController, context: Context) {
    val screenHeight = LocalConfiguration.current.screenHeightDp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((screenHeight * 0.5).dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.hockey),
                contentDescription = "background image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height((screenHeight * 0.25).dp),
                contentScale = ContentScale.FillWidth
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 20.dp, top = 20.dp)
            ) {
                Text(
                    text = "Ice Hockey Club",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(end = 20.dp)
                )
                TextButton(
                    onClick = { navController.navigate(NavRoutes.MembersScreen.route + "/false") },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = Color.Transparent
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "20 members")
                        Icon(Icons.Filled.NavigateNext, contentDescription = "arrow right", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            ClubLogo(modifier = Modifier)
            Spacer(modifier = Modifier.width(30.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CustomButton(
                text = "Manage Club",
                onClick = {
                    navController.navigate(NavRoutes.ClubManagementScreen.route)
                })
            CustomButton(
                text = "Share",
                onClick = {
                    Toast.makeText(context, "You are sharing the club", Toast.LENGTH_SHORT).show()
                })
        }
    }
}

@Composable
fun ClubDescription(description: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Description")
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = description, fontSize = 14.sp)
    }
}

@Composable
fun ClubSchedule() {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Schedule")
        Text(text = "Upcoming events", fontSize = 14.sp)
        Spacer(modifier = Modifier.height(20.dp))
        EventTile()
        Spacer(modifier = Modifier.height(20.dp))
        EventTile()
    }
}

@Composable
fun ClubLinks(listOfLinks: List<String>, context: Context) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Links")
        Column(verticalArrangement = Arrangement.Top) {
            listOfLinks.forEach { link ->
                ClubLinkRow(
                    link = link,
                    onClick = { Toast.makeText(context, link, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@Composable
fun ClubLinkRow(link: String, onClick: () -> Unit) {
    Text(
        text = link,
        color = linkBlue,
        modifier = Modifier
            .clickable { onClick() }
            .padding(5.dp))
}

@Composable
fun ClubContactInfo(name: String, phoneNumber: String, email: String) {
    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)) {
        ClubSectionTitle(text = "Contact Information")
        Text(text = name)
        Text(text = phoneNumber)
        Text(text = email)
    }
}

@Composable
fun ClubSectionTitle(text: String) {
    Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun EventTile() {

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hockey),
                    contentDescription = "Tile background",
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = "Ice Hockey Tournament",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 18.sp,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset.Zero,
                            blurRadius = 5f
                        )
                    )
                )
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = nokiaBlue),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        "Favourite icon",
                        tint = Color.White,
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                EventTileRowItem(icon = Icons.Outlined.CalendarMonth, iconDesc = "Calendar Icon", content = "12/12/2022" )
                EventTileRowItem(icon = Icons.Outlined.Timer, iconDesc = "Timer Icon", content = "19:00")
                EventTileRowItem(icon = Icons.Outlined.People, iconDesc = "People Icon", content = "5")
            }
        }

    }
}

@Composable
fun EventTileRowItem(icon: ImageVector, iconDesc: String, content: String) {
    Row() {
        Icon(icon, iconDesc)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = content)
    }
}

@Composable
fun ClubLogo(modifier: Modifier) {
    Card(
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Image(
            painter = painterResource(R.drawable.hockey_logo_with_sticks),
            contentDescription = "avatar",
            modifier = Modifier
                .padding(10.dp)
                .size(125.dp)
        )
    }
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = nokiaDarkBlue,
        contentColor = Color.White,
    )
) {
    Button(
        onClick = { onClick() },
        modifier = modifier
            .width(175.dp)
            .height(50.dp)
            .padding(5.dp),
        colors = colors,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = text, fontSize = 14.sp,)
    }
}