package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.MealDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@Composable
fun MenuScreen(
    menuName: String,
    mealsJson: String,
    onNavigateBack: () -> Unit
) {
    val meals: List<MealDto> = try {
        val type = object : TypeToken<List<MealDto>>() {}.type
        Gson().fromJson(mealsJson, type)
    } catch (e: Exception) {
        emptyList()
    }

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val userId by prefs.userId.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var favoriteMealIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun fetchFavorites() {
        val currentUserId = userId
        if (currentUserId != null) {
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.getMyFavorites(currentUserId)
                    if (response.isSuccessful) {
                        favoriteMealIds = response.body()?.map { it.mealId }?.toSet() ?: emptySet()
                    }
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
        }
    }

    fun toggleFavorite(mealId: Int) {
        val currentUserId = userId
        if (currentUserId == null) {
            Toast.makeText(context, "Morate biti prijavljeni", Toast.LENGTH_SHORT).show()
            return
        }

        val isCurrentlyFavorite = favoriteMealIds.contains(mealId)

        coroutineScope.launch {
            try {
                val response = if (isCurrentlyFavorite) {
                    RetrofitInstance.api.removeFavorite(currentUserId, FavoriteToggleDto(mealId))
                } else {
                    RetrofitInstance.api.addFavorite(currentUserId, FavoriteToggleDto(mealId))
                }

                if (response.isSuccessful) {
                    if (isCurrentlyFavorite) {
                        favoriteMealIds = favoriteMealIds - mealId
                    } else {
                        favoriteMealIds = favoriteMealIds + mealId
                    }
                } else {
                    Toast.makeText(context, "Greška: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(userId) {
        fetchFavorites()
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
                            text = menuName,
                            style = TextStyle(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                color = Color.White
                            ),
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
                    }
                }

                if (meals.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        items(meals) { meal ->
                            MealListItem(
                                meal = meal,
                                isFavorite = favoriteMealIds.contains(meal.mealId),
                                onToggleFavorite = { toggleFavorite(meal.mealId) })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Nema dostupnih jela za ovaj meni.")
                    }
                }
            }
        }
    }
}

@Composable
fun MealListItem(meal: MealDto, isFavorite: Boolean, onToggleFavorite: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%.2f EUR".format(meal.price),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        color = SpanRed
                    )
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Yellow else Color.Gray
                    )
                }
            }
        }
    }
}
