package com.example.smartmenza.ui.features

import MealDto
import android.util.Log
import android.widget.Toast
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
import com.example.core_ui.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MealCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items


@Composable
fun AllMealsScreen(
    onNavigateToMeal: (Int) -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var meals by remember { mutableStateOf<List<MealDto>>(emptyList()) }

    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val userId by prefs.userId.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var mealTypeNameMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var favoriteMealIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val mealsJson by remember {
        derivedStateOf { Gson().toJson(meals) }
    }

    fun fetchFavorites() {
        val currentUserId = userId
        if (currentUserId != null) {
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.getMyFavorites(currentUserId)
                    if (response.isSuccessful) {
                        favoriteMealIds = response.body()?.map { it.mealId }?.toSet() ?: emptySet()
                    }
                } catch (_: Exception) {
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
                    favoriteMealIds = if (isCurrentlyFavorite) favoriteMealIds - mealId else favoriteMealIds + mealId
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

    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val response = RetrofitInstance.api.getAllMeals()
            if (response.isSuccessful) {
                meals = response.body() ?: emptyList()
            } else {
                errorMessage = "Greška pri dohvaćanju jela: ${response.code()}"
                meals = emptyList()
            }
        } catch (e: Exception) {
            errorMessage = "Došlo je do greške: ${e.message}"
            meals = emptyList()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(mealsJson) {
        if (mealsJson.isBlank() || meals.isEmpty()) {
            mealTypeNameMap = emptyMap()
            return@LaunchedEffect
        }

        Log.d("MealTypeDebug", "mealsJson length=${mealsJson.length}")
        Log.d("MealTypeDebug", "mealsJson preview=${mealsJson.take(400)}")

        val parsedMeals: List<MealDto> = try {
            val type = object : TypeToken<List<MealDto>>() {}.type
            Gson().fromJson(mealsJson, type)
        } catch (e: Exception) {
            Log.e("MealTypeDebug", "Failed to parse mealsJson: ${e.message}", e)
            emptyList()
        }

        parsedMeals.forEach { meal ->
            Log.d(
                "MealTypeDebug",
                "Parsed meal: mealId=${meal.mealId}, mealTypeId=${meal.mealTypeId}, name=${meal.name}"
            )
        }

        val ids = parsedMeals
            .map { it.mealTypeId }
            .distinct()

        Log.d("MealTypeDebug", "Distinct mealTypeIds (filtered) = $ids")

        val map = mutableMapOf<Int, String>()

        ids.forEach { id ->
            try {
                Log.d("MealTypeDebug", "Requesting meal type for id=$id")
                val res = RetrofitInstance.api.getMealTypeName(id)

                Log.d(
                    "MealTypeDebug",
                    "Response for id=$id → code=${res.code()} successful=${res.isSuccessful}"
                )

                if (res.isSuccessful) {
                    val name = res.body()
                    Log.d("MealTypeDebug", "Body for id=$id → $name")
                    if (!name.isNullOrBlank()) map[id] = name
                } else {
                    val error = res.errorBody()?.string()
                    Log.e("MealTypeDebug", "Error for id=$id → code=${res.code()} body=$error")
                }
            } catch (e: Exception) {
                Log.e("MealTypeDebug", "Exception for id=$id → ${e.message}", e)
            }
        }

        Log.d("MealTypeDebug", "Final mealTypeNameMap = $map")
        mealTypeNameMap = map
    }


    SmartMenzaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBeige) {
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

                Spacer(modifier = Modifier.height(50.dp))

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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .offset(y = (-40).dp)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sva jela i pića",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))



                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            when {
                                isLoading -> {
                                    item {
                                        CircularProgressIndicator()
                                    }
                                }

                                errorMessage != null -> {
                                    item {
                                        Text(text = errorMessage!!, color = Color.Red)
                                    }
                                }

                                meals.isEmpty() -> {
                                    item {
                                        Text(text = "Nema dostupnih jela.")
                                    }
                                }

                                else -> {
                                    items(meals, key = { it.mealId }) { meal ->
                                        MealCard(
                                            name = meal.name,
                                            typeName = mealTypeNameMap[meal.mealTypeId] ?: "-",
                                            price = "%.2f EUR".format(meal.price),
                                            imageUrl = meal.imageUrl,
                                            isFavorite = favoriteMealIds.contains(meal.mealId),
                                            onToggleFavorite = { toggleFavorite(meal.mealId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = { onNavigateToMeal(meal.mealId) }
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
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