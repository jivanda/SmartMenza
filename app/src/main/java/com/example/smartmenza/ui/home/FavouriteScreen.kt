package com.example.smartmenza.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.R
import com.example.smartmenza.data.remote.MenuResponseDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MenuCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import kotlinx.coroutines.launch

@Composable
fun FavouriteScreen(
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    SmartMenzaTheme {
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var favoriteMenus by remember { mutableStateOf<List<MenuResponseDto>>(emptyList()) }

        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.getMyFavorites()

                    if (response.isSuccessful) {
                        favoriteMenus = response.body() ?: emptyList()
                    } else if (response.code() == 404) {
                        favoriteMenus = emptyList() // Treat 404 as no favorites
                    } else {
                        errorMessage = "Greška ${response.code()} pri dohvaćanju favorita."
                    }
                } catch (e: Exception) {
                    errorMessage = "Greška pri dohvaćanju favorita: ${e.message}"
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
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = "Favoriti",
                            style = TextStyle(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
                    }
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
                        // Show loading, error, or menus
                        when {
                            isLoading -> item { CircularProgressIndicator() }
                            errorMessage != null -> item { Text(text = errorMessage!!, color = Color.Red) }
                            favoriteMenus.isEmpty() -> item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nemate spremljenih favorita.")
                                }
                            }
                            else -> {
                                items(favoriteMenus) { menu ->
                                    val mealsText = menu.meals.joinToString("") { it.name }
                                    val totalPrice = menu.meals.sumOf { it.price }

                                    MenuCard(
                                        meals = mealsText,
                                        menuType = menu.name,
                                        price = "%.2f EUR".format(totalPrice),
                                        imageRes = R.drawable.hrenovke, // Replace with actual image if available
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
