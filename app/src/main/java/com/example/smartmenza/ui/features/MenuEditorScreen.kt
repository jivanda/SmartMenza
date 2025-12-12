package com.example.smartmenza.ui.features

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavController
import com.example.smartmenza.R
import com.example.smartmenza.data.remote.MealDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.data.remote.MenuMealItemDto
import com.example.smartmenza.data.remote.MenuWriteDto
import com.example.smartmenza.data.remote.MenuResponseDtoNoDate
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import kotlinx.coroutines.launch

sealed class MenuEditMode {
    object Create : MenuEditMode()
    data class Edit(val menuId: Int) : MenuEditMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuEditScreen(
    navController: NavController,
    mode: MenuEditMode,
    menuTypeOptions: List<MenuTypeOption>,
    initialName: String = "",
    initialDescription: String = "",
    initialMenuTypeId: Int? = null,
    initialSelectedMeals: List<MealDto?> = emptyList(),
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    val isEdit = mode is MenuEditMode.Edit
    val pageTitle = if (isEdit) "Uredi meni" else "Novi meni"
    val saveButtonLabel = if (isEdit) "Spremi promjene" else "Kreiraj meni"

    val scope = rememberCoroutineScope()

    // --- NEW: edit-prefill states ---
    var menuLoading by remember { mutableStateOf(false) }
    var menuLoadError by remember { mutableStateOf<String?>(null) }
    var pendingSelectedMeals by remember { mutableStateOf<List<MealDto>>(emptyList()) }

    // --- Form state ---
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    var selectedMenuTypeId by remember {
        mutableStateOf(initialMenuTypeId ?: menuTypeOptions.firstOrNull()?.id ?: 0)
    }
    var menuTypeExpanded by remember { mutableStateOf(false) }

    // --- Meals list from backend ---
    var allMeals by remember { mutableStateOf<List<MealDto>>(emptyList()) }
    var mealsLoading by remember { mutableStateOf(true) }
    var mealsError by remember { mutableStateOf<String?>(null) }

    // Exactly 5 slots
    val normalizedInitialMeals = remember(initialSelectedMeals) {
        (initialSelectedMeals + List(5) { null }).take(5)
    }
    val mealSelections = remember {
        mutableStateListOf<MealDto?>().apply { addAll(normalizedInitialMeals) }
    }

    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    // Fetch all meals once
    LaunchedEffect(Unit) {
        mealsLoading = true
        mealsError = null
        try {
            val response = RetrofitInstance.api.getAllMeals()
            if (response.isSuccessful) {
                allMeals = response.body().orEmpty()
            } else {
                mealsError = "Greška pri dohvaćanju jela: ${response.code()}"
            }
        } catch (e: Exception) {
            mealsError = "Greška pri dohvaćanju jela: ${e.message}"
        } finally {
            mealsLoading = false
        }
    }

    // --- NEW: Fetch menu when editing (prefill name/desc/type + store meals) ---
    LaunchedEffect(mode) {
        if (mode is MenuEditMode.Edit) {
            menuLoading = true
            menuLoadError = null
            try {
                val res = RetrofitInstance.api.getMenuById(mode.menuId) // <-- add to SmartMenzaApi
                if (res.isSuccessful) {
                    val menu: MenuResponseDtoNoDate = res.body()!!

                    name = menu.name
                    description = menu.description.orEmpty()

                    // Map menuTypeName -> id using menuTypeOptions labels
                    val matchedType = menuTypeOptions.firstOrNull {
                        it.label.equals(menu.menuTypeName ?: "", ignoreCase = true)
                    }
                    selectedMenuTypeId = matchedType?.id ?: (menuTypeOptions.firstOrNull()?.id ?: 0)

                    pendingSelectedMeals = menu.meals
                } else {
                    menuLoadError = when (res.code()) {
                        404 -> "Meni nije pronađen."
                        else -> "Greška (${res.code()}) pri dohvaćanju menija."
                    }
                }
            } catch (e: Exception) {
                menuLoadError = "Greška pri dohvaćanju menija: ${e.message}"
            } finally {
                menuLoading = false
            }
        }
    }

    // --- NEW: Once allMeals is loaded, map pending meals into the 5 slots ---
    LaunchedEffect(allMeals, pendingSelectedMeals) {
        if (allMeals.isNotEmpty() && pendingSelectedMeals.isNotEmpty()) {
            val selected = pendingSelectedMeals
                .mapNotNull { sel -> allMeals.firstOrNull { it.mealId == sel.mealId } }
                .take(5)

            for (i in 0 until 5) {
                mealSelections[i] = selected.getOrNull(i)
            }

            pendingSelectedMeals = emptyList()
        }
    }

    val selectedMealsCount = mealSelections.count { it != null }
    val isFormValid = name.isNotBlank() && selectedMenuTypeId != 0 && selectedMealsCount >= 3

    fun buildWriteDto(): MenuWriteDto {
        val mealItems = mealSelections
            .filterNotNull()
            .map { MenuMealItemDto(mealId = it.mealId) }

        return MenuWriteDto(
            name = name.trim(),
            description = description.trim().ifBlank { null },
            menuTypeId = selectedMenuTypeId,
            meals = mealItems
        )
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
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = pageTitle, style = MaterialTheme.typography.headlineLarge)
                        Spacer(modifier = Modifier.height(16.dp))

                        // NEW: menu load indicator / error
                        if (menuLoading) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        menuLoadError?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Naziv
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Naziv menija:",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.widthIn(min = 120.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Opis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Opis:",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.widthIn(min = 120.dp).padding(top = 8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                                maxLines = 4
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tip menija
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tip menija:",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.widthIn(min = 120.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            val currentTypeLabel = menuTypeOptions
                                .firstOrNull { it.id == selectedMenuTypeId }
                                ?.label ?: "Odaberi tip"

                            ExposedDropdownMenuBox(
                                expanded = menuTypeExpanded,
                                onExpandedChange = { menuTypeExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = currentTypeLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuTypeExpanded)
                                    },
                                    enabled = false,
                                    colors = TextFieldDefaults.colors(
                                        disabledTextColor = LocalContentColor.current
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = menuTypeExpanded,
                                    onDismissRequest = { menuTypeExpanded = false }
                                ) {
                                    menuTypeOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.label) },
                                            onClick = {
                                                selectedMenuTypeId = option.id
                                                menuTypeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Odaberi jela (minimalno 3):",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        when {
                            mealsLoading -> CircularProgressIndicator()

                            mealsError != null -> Text(
                                text = mealsError!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )

                            else -> {
                                mealSelections.forEachIndexed { index, selectedMeal ->
                                    MealDropdownRow(
                                        index = index,
                                        allMeals = allMeals,
                                        selectedMeal = selectedMeal,
                                        onMealSelected = { mealSelections[index] = it }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        validationMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        saveError?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() },
                                enabled = !saving
                            ) {
                                Text("Odustani")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
                                    if (!isFormValid) {
                                        validationMessage =
                                            "Naziv mora biti unesen i minimalno 3 jela moraju biti odabrana."
                                        return@Button
                                    }

                                    validationMessage = null
                                    saveError = null

                                    scope.launch {
                                        saving = true
                                        try {
                                            val roleHeader = "Employee"
                                            val dto = buildWriteDto()

                                            val response = when (mode) {
                                                is MenuEditMode.Create ->
                                                    RetrofitInstance.api.createMenu(role = roleHeader, dto = dto)

                                                is MenuEditMode.Edit ->
                                                    RetrofitInstance.api.updateMenu(
                                                        menuId = mode.menuId,
                                                        role = roleHeader,
                                                        dto = dto
                                                    )
                                            }

                                            if (response.isSuccessful) {
                                                navController.popBackStack()
                                            } else {
                                                saveError = when (response.code()) {
                                                    403 -> "Nemate ovlasti za ovu radnju."
                                                    400 -> response.errorBody()?.string() ?: "Podaci nisu ispravni."
                                                    else -> "Greška (${response.code()}) pri spremanju menija."
                                                }
                                            }
                                        } catch (e: Exception) {
                                            saveError = "Greška pri spremanju menija: ${e.message}"
                                        } finally {
                                            saving = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpanRed,
                                    contentColor = Color.White
                                ),
                                enabled = isFormValid &&
                                        !saving &&
                                        !menuLoading &&
                                        menuLoadError == null &&
                                        !mealsLoading &&
                                        mealsError == null
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(saveButtonLabel)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    Text(
                        text = "Powered by SPAN",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealDropdownRow(
    index: Int,
    allMeals: List<MealDto>,
    selectedMeal: MealDto?,
    onMealSelected: (MealDto?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Jelo ${index + 1}:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.widthIn(min = 80.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMeal?.name ?: "Nije odabrano",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledTextColor = LocalContentColor.current
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("— Nijedno —") },
                    onClick = {
                        onMealSelected(null)
                        expanded = false
                    }
                )

                allMeals.forEach { meal ->
                    DropdownMenuItem(
                        text = { Text(meal.name) },
                        onClick = {
                            onMealSelected(meal)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
