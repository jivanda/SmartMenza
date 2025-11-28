package com.example.smartmenza.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import com.example.smartmenza.ui.components.GridItem
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.components.StickerCard
import com.example.smartmenza.ui.components.IconGrid
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*      // rounded versions
import androidx.compose.material.icons.sharp.*        // sharp versions
import androidx.compose.material.icons.twotone.*      // two-tone versions
import com.example.smartmenza.ui.components.MenuCard


@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) { SmartMenzaTheme {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val userName by prefs.userName.collectAsState(initial = "Korisnik")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBeige
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                    .background(SpanRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SmartMenza",
                    style = TextStyle(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = subtlePattern,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.06f),
                    contentScale = ContentScale.Crop
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            var menuExpanded by remember { mutableStateOf(false) }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = 4.dp,
                                            color = SpanRed,
                                            shape = CircleShape
                                        )
                                        .clickable { menuExpanded = true }
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.profile),
                                        contentDescription = "User menu",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "Odjava",
                                                color = SpanRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onLogout()
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Dobrodošli, $userName 👋",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = SpanRed
                                )
                            )
                        }

                        StickerCard(
                            imageRes = R.drawable.becki,
                            cardTypeText = "AI Preporuka",
                            title = "Bečki odrezak",
                            description = "Bečki odrezak sa pilećim mesom.",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {}
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val menuItems = listOf(
                            GridItem(Icons.Filled.LunchDining, "Jelovnik", onClick = {}),
                            GridItem(Icons.Filled.Flag, "Ciljevi", onClick = {}),
                            GridItem(Icons.Filled.Favorite, "Favoriti", onClick = {}),
                            GridItem(Icons.Filled.ShowChart, "Statistika", onClick = {})
                        )

                        IconGrid(
                            items = menuItems,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Današnja Ponuda",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                color = SpanRed
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Doručak",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        MenuCard(
                            meals = "Hrenovke (x2)\nVoda\nČokoladni puding",
                            menuType = "Meni 1",
                            price = "1 EUR",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
}