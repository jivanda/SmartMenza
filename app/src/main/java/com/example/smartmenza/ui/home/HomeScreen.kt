package com.example.smartmenza.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.smartmenza.data.remote.MenuResponseDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.IconGrid
import com.example.smartmenza.ui.components.GridItem
import com.example.smartmenza.ui.components.StickerCard
import com.example.smartmenza.ui.components.MenuCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    onAllMeals: () -> Unit = {},
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    SmartMenzaTheme {
        val context = LocalContext.current
        val prefs = remember { UserPreferences(context) }
        val userName by prefs.userName.collectAsState(initial = "Korisnik")
        val userRole by prefs.userRole.collectAsState(initial = "Student")

        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var todayMenus by remember { mutableStateOf<List<MenuResponseDto>>(emptyList()) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    val dateStr = LocalDate.now().format(formatter)

                    val response = RetrofitInstance.api.getMenusByDate(dateStr)

                    if (response.isSuccessful) {
                        todayMenus = response.body() ?: emptyList()
                    } else {
                        errorMessage = "Greška ${response.code()} pri dohvaćanju menija."
                    }
                } catch (e: Exception) {
                    errorMessage = "Greška pri dohvaćanju menija: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundBeige
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
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

                    // Background pattern
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
                            // User row
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
                                            .background(Color.LightGray)
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
                                            text = { Text("Odjava", color = SpanRed, fontWeight = FontWeight.Bold) },
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

                            // StickerCard
                            StickerCard(
                                imageRes = R.drawable.becki,
                                cardTypeText = "AI Preporuka",
                                title = "Bečki odrezak",
                                description = "Bečki odrezak sa pilećim mesom.",
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {}
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val menuItems = when (userRole) {
                                "Student" -> listOf(
                                    GridItem(Icons.Filled.LunchDining, "Jelovnik", onClick = {onAllMeals()}),
                                    GridItem(Icons.Filled.Flag, "Ciljevi", onClick = {}),
                                    GridItem(Icons.Filled.Favorite, "Favoriti", onClick = {}),
                                    GridItem(Icons.Filled.Star, "Ponuda", onClick = {})
                                )

                                "Employee" -> listOf(
                                    GridItem(Icons.Filled.LunchDining, "Jelovnik", onClick = {onAllMeals()}),
                                    GridItem(Icons.Filled.RestaurantMenu, "Meniji", onClick = {}),
                                    GridItem(Icons.Filled.ShowChart, "Statistika", onClick = {}),
                                    GridItem(Icons.Filled.Star, "Ponuda", onClick = {})
                                )

                                else -> emptyList()
                            }


                            IconGrid(
                                items = menuItems,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Današnja ponuda - " + LocalDate.now()
                                    .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = SpanRed
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Show loading, error, or menus
                        when {
                            isLoading -> item { CircularProgressIndicator() }
                            errorMessage != null -> item { Text(text = errorMessage!!, color = Color.Red) }
                            todayMenus.isEmpty() -> item { Text("Za današnji datum nema spremljenih menija.") }
                            else -> {
                                val categoryOrder = listOf("Dorucak", "Rucak", "Vecera")
                                val comparator = compareBy<String?> { menuTypeName ->
                                    if (menuTypeName == null) {
                                        categoryOrder.size + 1 // Nulls go last
                                    } else {
                                        val index = categoryOrder.indexOfFirst { it.equals(menuTypeName, ignoreCase = true) }
                                        if (index != -1) index else categoryOrder.size // Known categories first, then others
                                    }
                                }
                                val groupedMenus = todayMenus.groupBy { it.menuTypeName }.toSortedMap(comparator)

                                groupedMenus.forEach { (menuTypeName, menus) ->
                                    item {
                                        Text(
                                            text = menuTypeName ?: "Jelovnik",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = Montserrat,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }

                                    val mergedMenus = menus.groupBy { it.name }
                                        .map { (_, menusWithSameName) ->
                                            val allMeals = menusWithSameName.flatMap { it.meals }
                                            menusWithSameName.first().copy(meals = allMeals)
                                        }

                                    items(mergedMenus) { menu ->
                                        val mealsText = menu.meals.joinToString("") { it.name }
                                        val totalPrice = menu.meals.sumOf { it.price }

                                        MenuCard(
                                            meals = mealsText,
                                            menuType = menu.name,
                                            price = "%.2f EUR".format(totalPrice),
                                            imageRes = R.drawable.hrenovke,
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = {}
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
