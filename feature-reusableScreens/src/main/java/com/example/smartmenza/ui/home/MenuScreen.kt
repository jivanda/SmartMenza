package com.example.smartmenza.ui.home

import MealDto
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MealCard
import com.example.smartmenza.ui.theme.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import com.example.core_ui.R
import com.example.smartmenza.data.remote.NutritionAssessmentDto
import com.example.smartmenza.data.remote.NutritionResultDto


@Composable
fun MenuScreen(
    menuId: Int,
    menuName: String,
    mealsJson: String,
    onNavigateToMeal: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
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
    val role by prefs.userRole.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    var nutrition by remember { mutableStateOf<NutritionResultDto?>(null) }
    var assessment by remember { mutableStateOf<NutritionAssessmentDto?>(null) }
    var isAiLoading by remember { mutableStateOf(true) }

    var mealTypeNameMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
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
                } catch (_: Exception) {}
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
                val response = if (isCurrentlyFavorite)
                    RetrofitInstance.api.removeFavorite(currentUserId, FavoriteToggleDto(mealId))
                else
                    RetrofitInstance.api.addFavorite(currentUserId, FavoriteToggleDto(mealId))

                if (response.isSuccessful) {
                    favoriteMealIds = if (isCurrentlyFavorite)
                        favoriteMealIds - mealId
                    else
                        favoriteMealIds + mealId
                } else {
                    Toast.makeText(context, "Greška: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(userId) { fetchFavorites() }

    LaunchedEffect(mealsJson) {
        val parsedMeals: List<MealDto> = try {
            val type = object : TypeToken<List<MealDto>>() {}.type
            Gson().fromJson(mealsJson, type)
        } catch (e: Exception) {
            emptyList()
        }

        val ids = parsedMeals.map { it.mealTypeId }.distinct()
        val map = mutableMapOf<Int, String>()
        ids.forEach { id ->
            try {
                val res = RetrofitInstance.api.getMealTypeName(id)
                if (res.isSuccessful) res.body()?.let { name -> map[id] = name }
            } catch (_: Exception) {}
        }
        mealTypeNameMap = map
    }

    LaunchedEffect(meals, role) {
        if (role == "Employee") {
            isAiLoading = true
            try {
                val nutritionResponse = RetrofitInstance.api.analyzeMenuNutrition(menuId)
                if (nutritionResponse.isSuccessful) {
                    nutrition = nutritionResponse.body()
                }

                val assessmentResponse = RetrofitInstance.api.assessMenuHealth(menuId)
                if (assessmentResponse.isSuccessful) {
                    assessment = assessmentResponse.body()
                }
            } catch (_: Exception) {
                nutrition = null
                assessment = null
            } finally {
                isAiLoading = false
            }
        } else {
            nutrition = null
            assessment = null
            isAiLoading = false
        }
    }

    SmartMenzaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBeige) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(SpanRed),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
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
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = subtlePattern,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().alpha(0.06f),
                        contentScale = ContentScale.Crop
                    )

                    if (meals.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .offset(y = (-40).dp)
                                .padding(horizontal = 1.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(25.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                item{
                                    Spacer(modifier = Modifier.height(8.dp))}
                                item{
                            Text("Jela koja ovaj meni sadrži:")}
                                item{
                            Spacer(modifier = Modifier.height(8.dp))}

                            meals.forEach { meal ->

                                item {
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
                                }
                                item{
                                Spacer(modifier = Modifier.height(8.dp))}
                            }


                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                item{
                            Text("Cijena menija: %.2f EUR".format(meals.sumOf { it.price }))}

                            if (role == "Employee") {

                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(
                                                0xFFF5F5F5
                                            )
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            when {
                                                isAiLoading -> {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(80.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        CircularProgressIndicator()
                                                    }
                                                }

                                                nutrition != null -> {
                                                    Text(
                                                        "Nutritivne vrijednosti menija",
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 18.sp
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Kalorije: ${nutrition!!.calories} kcal")
                                                    Text("Proteini: ${nutrition!!.proteins} g")
                                                    Text("Ugljikohidrati: ${nutrition!!.carbohydrates} g")
                                                    Text("Masti: ${nutrition!!.fats} g")

                                                    assessment?.let { a ->
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        Text(
                                                            "Procjena zdravlja menija",
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 18.sp
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(a.reasoning)
                                                    }
                                                }

                                                else -> {
                                                    Text("AI analiza nije dostupna")
                                                }
                                            }
                                        }
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
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nema dostupnih jela za ovaj meni.")
                        }
                    }
                }
            }
        }
    }
}

