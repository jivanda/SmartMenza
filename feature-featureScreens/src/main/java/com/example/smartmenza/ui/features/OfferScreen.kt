package com.example.smartmenza.ui.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
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
import com.example.smartmenza.data.remote.MenuResponseDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MenuCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.google.gson.Gson
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferScreen(
    onNavigateToMenu: (menuId: Int) -> Unit,
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    SmartMenzaTheme {
        var selectedDate by remember { mutableStateOf(LocalDate.now()) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var todayMenus by remember { mutableStateOf<List<MenuResponseDto>>(emptyList()) }
        var showDatePicker by remember { mutableStateOf(false) }

        val apiFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
        val displayFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

        LaunchedEffect(selectedDate) {
            isLoading = true
            errorMessage = null
            try {
                val dateStr = selectedDate.format(apiFormatter)
                val response = RetrofitInstance.api.getMenusByDate(dateStr)

                if (response.isSuccessful) {
                    todayMenus = response.body() ?: emptyList()
                } else {
                    if (response.code() == 404) {
                        // Friendly user-facing message for missing menus
                        todayMenus = emptyList()
                        errorMessage = "Za odabrani datum nije kreiran nijedan meni."
                    } else {
                        errorMessage = "Greška ${response.code()} pri dohvaćanju menija."
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Greška pri dohvaćanju menija: ${e.message}"
            } finally {
                isLoading = false
            }
        }


        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BackgroundBeige
        ) {
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

                Spacer(modifier = Modifier.height(50.dp))

                Box(modifier = Modifier.fillMaxSize()) {
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
                            text = "Ponuda",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Datum: ${selectedDate.format(displayFormatter)}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { showDatePicker = true }) {
                                Text("Promijeni datum")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            when {
                                isLoading -> item {
                                    CircularProgressIndicator()
                                }

                                errorMessage != null -> item {
                                    Text(text = errorMessage!!, color = Color.Red)
                                }

                                todayMenus.isEmpty() -> item {
                                    Text("Za odabrani datum nema spremljenih menija.")
                                }

                                else -> {
                                    val categoryOrder = listOf("Dorucak", "Rucak", "Vecera")
                                    val comparator = compareBy<String?> { menuTypeName ->
                                        if (menuTypeName == null) {
                                            categoryOrder.size + 1 // Nulls go last
                                        } else {
                                            val index = categoryOrder.indexOfFirst {
                                                it.equals(menuTypeName, ignoreCase = true)
                                            }
                                            if (index != -1) index else categoryOrder.size
                                        }
                                    }

                                    val groupedMenus =
                                        todayMenus.groupBy { it.menuTypeName }.toSortedMap(
                                            comparator
                                        )

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
                                                val allMeals =
                                                    menusWithSameName.flatMap { it.meals }
                                                menusWithSameName.first().copy(meals = allMeals)
                                            }

                                        items(mergedMenus) { menu ->
                                            val mealsText =
                                                menu.meals.joinToString("\n") { it.name }
                                            val totalPrice =
                                                menu.meals.sumOf { it.price }

                                            var mainDishImageUrl: String? = null

                                            for (meal in menu.meals) {
                                                if (meal.mealTypeId == 1) {
                                                    mainDishImageUrl = meal.imageUrl
                                                    break
                                                }
                                            }

                                            MenuCard(
                                                meals = mealsText,
                                                menuType = menu.name,
                                                price = "%.2f EUR".format(totalPrice),
                                                imageUrl = mainDishImageUrl,
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = {
                                                    onNavigateToMenu(menu.menuId)
                                                },
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
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

        // --- DATE PICKER DIALOG ---
        if (showDatePicker) {
            val initialMillis = remember(selectedDate) {
                selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialMillis
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val millis = datePickerState.selectedDateMillis
                            if (millis != null) {
                                val newDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                selectedDate = newDate
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Potvrdi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Odustani")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
