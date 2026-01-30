package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import coil.compose.AsyncImage
import com.example.core_ui.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteMealDto
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.RetrofitInstance
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
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val userId by prefs.userId.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var favoriteMeals by remember { mutableStateOf<List<FavoriteMealDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun fetchFavorites() {
        val currentUserId = userId!!
        coroutineScope.launch {
            isLoading = true
            try {
                val response = RetrofitInstance.api.getMyFavorites(currentUserId)
                if (response.isSuccessful) {
                    favoriteMeals = response.body() ?: emptyList()
                } else if (response.code() == 404) {
                    favoriteMeals = emptyList() // No favorites found
                } else {
                    errorMessage = "Greška pri dohvaćanju favorita: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Greška: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun removeFavorite(mealId: Int) {
        val currentUserId = userId
        if (currentUserId != null) {
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.removeFavorite(currentUserId, FavoriteToggleDto(mealId))
                    if (response.isSuccessful) {
                        fetchFavorites() // Refresh the list
                        Toast.makeText(context, "Jelo uklonjeno iz favorita", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Greška: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            fetchFavorites()
        } else {
            isLoading = false
            favoriteMeals = emptyList()
        }
    }

    SmartMenzaTheme {
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                        Spacer(modifier = Modifier.width(48.dp))
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

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = errorMessage!!, color = Color.Red)
                        }
                    } else if (favoriteMeals.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nemate spremljenih favorita.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            items(favoriteMeals) { meal ->
                                FavoriteMealCard(meal = meal, onRemove = { removeFavorite(meal.mealId) })
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }

                    Text(
                        text = "Powered by SPAN",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteMealCard(meal: FavoriteMealDto, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = meal.imageUrl ?: R.drawable.ic_launcher_background, // Fallback image
                contentDescription = meal.mealName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Kalorije: ${meal.calories}", style = MaterialTheme.typography.bodyMedium)
                Text("Proteini: ${meal.protein}g", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = SpanRed)
            }
        }
    }
}
