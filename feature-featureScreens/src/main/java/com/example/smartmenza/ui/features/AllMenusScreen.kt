package com.example.smartmenza.ui.features

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core_ui.R
import com.example.smartmenza.data.remote.MenuResponseDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.MenuSelectableCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import com.google.gson.Gson
import kotlinx.coroutines.launch

data class MenuTypeOption(
    val id: Int,
    val label: String
)

@Composable
fun AllMenusScreen(
    onNavigateToMenu: (menuId: Int) -> Unit,
    onCreateMenu: () -> Unit,
    onEditMenu: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
)
 {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var menus by remember { mutableStateOf<List<MenuResponseDto>>(emptyList()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var menuToDelete by remember { mutableStateOf<MenuResponseDto?>(null) }

    val scope = rememberCoroutineScope()

    val menuTypeOptions = listOf(
        MenuTypeOption(1, "Doručak"),
        MenuTypeOption(2, "Ručak"),
        MenuTypeOption(3, "Večera")
    )

    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(menuTypeOptions.first()) }

    LaunchedEffect(selectedType.id) {
        isLoading = true
        errorMessage = null
        try {
            val response = RetrofitInstance.api.getMenusByType(menuTypeId = selectedType.id)

            if (response.isSuccessful) {
                menus = response.body() ?: emptyList()
            } else {
                errorMessage = "Greška pri dohvaćanju menija: ${response.code()}"
                menus = emptyList()
            }
        } catch (e: Exception) {
            errorMessage = "Došlo je do greške: ${e.message}"
            menus = emptyList()
        } finally {
            isLoading = false
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
                            text = "Uređivanje menija",
                            style = MaterialTheme.typography.headlineLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Odaberi tip menija:",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Box {
                                Text(
                                    text = selectedType.label,
                                    modifier = Modifier
                                        .clickable { expanded = true }
                                        .padding(8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    menuTypeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                expanded = false
                                                selectedType = option
                                                // LaunchedEffect will trigger fetch
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = onCreateMenu,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpanRed,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Text(
                                    text = "+ Dodaj novi",
                                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                                )
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

                                menus.isEmpty() -> item {
                                    Text("Za odabrani tip menija nema spremljenih menija.")
                                }

                                else -> {
                                    val mergedMenus = menus.groupBy { it.name }
                                        .map { (_, menusWithSameName) ->
                                            val allMeals =
                                                menusWithSameName.flatMap { it.meals }
                                            menusWithSameName.first().copy(meals = allMeals)
                                        }

                                    items(mergedMenus) { menu ->
                                        val mealsText = menu.meals.joinToString("\n") { it.name }
                                        val totalPrice = menu.meals.sumOf { it.price }

                                        var mainDishImageUrl: String? = null

                                        for (meal in menu.meals) {
                                            if (meal.mealTypeId == 1) {
                                                mainDishImageUrl = meal.imageUrl
                                                break
                                            }
                                        }

                                        MenuSelectableCard(
                                            meals = mealsText,
                                            menuType = menu.name,
                                            price = "%.2f EUR".format(totalPrice),
                                            imageUrl = mainDishImageUrl,
                                            modifier = Modifier.fillMaxWidth(),
                                            onInfoClick = {
                                                onNavigateToMenu(menu.menuId)
                                            },
                                            onEditClick = { onEditMenu(menu.menuId) },
                                            onDeleteClick = {
                                                menuToDelete = menu
                                                showDeleteDialog = true
                                            }
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))
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

            if (showDeleteDialog && menuToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        menuToDelete = null
                    },
                    title = {
                        Text("Brisanje menija")
                    },
                    text = {
                        Text("Jeste li sigurni da želite obrisati '${menuToDelete!!.name}'?")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val menu = menuToDelete
                                if (menu != null) {
                                    scope.launch {
                                        try {
                                            val response = RetrofitInstance.api
                                                .deleteMenu(menu.menuId, "Employee")

                                            if (response.isSuccessful) {
                                                menus = menus.filter { it.menuId != menu.menuId }
                                            } else {
                                                errorMessage = when (response.code()) {
                                                    401, 403 -> "Nemate ovlasti za brisanje ovog menija."
                                                    400 -> response.body()?.message
                                                        ?: "Greška pri brisanju menija."
                                                    else -> "Greška pri brisanju menija: ${response.code()}"
                                                }
                                            }
                                        } catch (e: Exception) {
                                            errorMessage = "Greška pri brisanju menija: ${e.message}"
                                        } finally {
                                            showDeleteDialog = false
                                            menuToDelete = null
                                        }
                                    }
                                } else {
                                    showDeleteDialog = false
                                    menuToDelete = null
                                }
                            }
                        ) {
                            Text("Obriši", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                menuToDelete = null
                            }
                        ) {
                            Text("Odustani")
                        }
                    }
                )
            }
        }
    }
}
