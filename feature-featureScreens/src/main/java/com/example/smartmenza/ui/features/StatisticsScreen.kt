package com.example.smartmenza.ui.features

import MealDto
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import com.example.smartmenza.data.remote.MealRatingStatsDto
import com.example.smartmenza.data.remote.OverallStatsDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MealStatisticsCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateToMeal: (Int) -> Unit,
    onNavigateBack: () -> Unit,
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

    var statsLoading by remember { mutableStateOf(false) }
    var statsError by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf<OverallStatsDto?>(null) }

    var mealRatingMap by remember { mutableStateOf<Map<Int, MealRatingStatsDto>>(emptyMap()) }

    val isoFmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val hrFmt = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    var dateFrom by remember { mutableStateOf(LocalDate.now().minusYears(1)) }
    var dateTo by remember { mutableStateOf(LocalDate.now()) }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    val fromPickerState = rememberDatePickerState()
    val toPickerState = rememberDatePickerState()

    fun popularityFor(mealId: Int): Double {
        val r = mealRatingMap[mealId]
        val reviews = r?.numberOfReviews ?: 0
        val avg = r?.averageRating ?: 0.0
        return reviews.toDouble() + avg
    }

    fun millisToLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun fetchOverallStats() {
        statsLoading = true
        statsError = null
        coroutineScope.launch {
            try {
                val res = RetrofitInstance.api.getOverallStats(
                    dateFrom.format(isoFmt),
                    dateTo.format(isoFmt)
                )
                if (res.isSuccessful) {
                    stats = res.body()
                } else {
                    statsError = "Greška statistike: ${res.code()}"
                }
            } catch (e: Exception) {
                statsError = "Greška statistike: ${e.message}"
            } finally {
                statsLoading = false
            }
        }
    }

    fun fetchMealRatingStats() {
        coroutineScope.launch {
            try {
                val res = RetrofitInstance.api.getMealRatingStats()
                if (res.isSuccessful) {
                    val list = res.body() ?: emptyList()
                    mealRatingMap = list.associateBy { it.mealId }
                } else {
                    Log.e("Stats", "getMealRatingStats failed: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("Stats", "getMealRatingStats exception: ${e.message}", e)
            }
        }
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
                } catch (_: Exception) { }
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
                    favoriteMealIds =
                        if (isCurrentlyFavorite) favoriteMealIds - mealId else favoriteMealIds + mealId
                } else {
                    Toast.makeText(context, "Greška: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(userId) { fetchFavorites() }

    LaunchedEffect(Unit) {
        fetchOverallStats()
        fetchMealRatingStats()

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

    val mealsJson by remember { derivedStateOf { Gson().toJson(meals) } }

    LaunchedEffect(mealsJson) {
        if (mealsJson.isBlank() || meals.isEmpty()) {
            mealTypeNameMap = emptyMap()
            return@LaunchedEffect
        }

        val parsedMeals: List<MealDto> = try {
            val type = object : TypeToken<List<MealDto>>() {}.type
            Gson().fromJson(mealsJson, type)
        } catch (e: Exception) {
            Log.e("MealTypeDebug", "Failed to parse mealsJson: ${e.message}", e)
            emptyList()
        }

        val ids = parsedMeals.map { it.mealTypeId }.distinct()
        val map = mutableMapOf<Int, String>()

        ids.forEach { id ->
            try {
                val res = RetrofitInstance.api.getMealTypeName(id)
                if (res.isSuccessful) {
                    val name = res.body()
                    if (!name.isNullOrBlank()) map[id] = name
                }
            } catch (_: Exception) { }
        }

        mealTypeNameMap = map
    }

    if (showFromPicker) {
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = fromPickerState.selectedDateMillis
                    if (millis != null) dateFrom = millisToLocalDate(millis)
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Odustani") }
            }
        ) { DatePicker(state = fromPickerState) }
    }

    if (showToPicker) {
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = toPickerState.selectedDateMillis
                    if (millis != null) dateTo = millisToLocalDate(millis)
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Odustani") }
            }
        ) { DatePicker(state = toPickerState) }
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
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                            text = "SmartMenza",
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
                    Image(
                        painter = subtlePattern,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().alpha(0.06f),
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
                        Spacer(modifier = Modifier.height(48.dp))

                        Text(text = "Statistika", style = MaterialTheme.typography.headlineLarge)

                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            val totalMealsText = stats?.totalMeals?.toString() ?: "-"
                            val avgText =
                                stats?.overallAverageRating?.let { String.format("%.2f", it) }
                                    ?: "-"
                            val maxText = stats?.maxRating?.let { String.format("%.2f", it) } ?: "-"

                            Text(
                                "Ukupno jela: $totalMealsText",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Prosječna ocjena: $avgText",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Najveća ocjena: $maxText",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (statsLoading) {
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                            if (statsError != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(text = statsError!!, color = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtriraj po vremenu:",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = { showFromPicker = true },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(dateFrom.format(hrFmt))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { showToPicker = true },
                                modifier = Modifier.weight(1f).height(56.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(dateTo.format(hrFmt))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (dateFrom.isAfter(dateTo)) {
                                    Toast.makeText(
                                        context,
                                        "Datum OD ne može biti poslije datuma DO.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    fetchOverallStats()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpanRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                "Primijeni",
                                style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                when {
                                    isLoading ->
                                        item { CircularProgressIndicator() }

                                    errorMessage != null ->
                                        item {
                                            Text(
                                                text = errorMessage!!,
                                                color = Color.Red
                                            )
                                        }

                                    meals.isEmpty() ->
                                        item { Text(text = "Nema dostupnih jela.") }

                                    else -> {
                                        val sortedMeals = meals
                                            .sortedByDescending { meal -> popularityFor(meal.mealId) }

                                        sortedMeals.forEach { meal ->
                                            val r = mealRatingMap[meal.mealId]

                                            val numberOfReviews = r?.numberOfReviews ?: 0
                                            val averageRating = r?.averageRating ?: 0.0
                                            val popularnost =
                                                numberOfReviews.toDouble() + averageRating

                                            item {
                                                MealStatisticsCard(
                                                    name = meal.name,
                                                    typeName = mealTypeNameMap[meal.mealTypeId]
                                                        ?: "-",
                                                    price = "%.2f EUR".format(meal.price),
                                                    imageUrl = meal.imageUrl,
                                                    isFavorite = favoriteMealIds.contains(meal.mealId),
                                                    onToggleFavorite = { toggleFavorite(meal.mealId) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    onClick = { onNavigateToMeal(meal.mealId) },
                                                    numberOfReviews = numberOfReviews,
                                                    averageRating = averageRating
                                                )
                                            }

                                            //Text("Popularnost: ${String.format("%.2f", popularnost)}")

                                            item {
                                                Spacer(modifier = Modifier.height(8.dp))
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
                }
            }
        }
    }
}