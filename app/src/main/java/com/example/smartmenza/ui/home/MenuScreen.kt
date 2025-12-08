package com.example.smartmenza.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.data.remote.MealDto
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                            MealListItem(meal = meal)
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
fun MealListItem(meal: MealDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = meal.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = "%.2f EUR".format(meal.price),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    color = SpanRed
                )
            )
        }
    }
}
