package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.GoalCreateDto
import com.example.smartmenza.data.remote.GoalDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.GoalCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import kotlinx.coroutines.launch
import com.example.core_ui.R

private data class PendingGoalUpdate(
    val goalId: Int,
    val calories: Int,
    val proteins: Double,
    val carbs: Double,
    val fat: Double
)

@Composable
fun GoalScreen(
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var goals by remember { mutableStateOf<List<GoalDto>>(emptyList()) }

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<GoalDto?>(null) }
    var goalToEdit by remember { mutableStateOf<GoalDto?>(null) }
    var pendingGoalUpdate by remember { mutableStateOf<PendingGoalUpdate?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }
    val userId by prefs.userId.collectAsState(initial = null)

    fun fetchGoals() {
        val currentUserId = userId
        if (currentUserId != null) {
            coroutineScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val response = RetrofitInstance.api.getMyGoals(currentUserId)
                    goals = when {
                        response.isSuccessful -> response.body() ?: emptyList()
                        response.code() == 404 -> emptyList()
                        else -> {
                            errorMessage = "Greška ${response.code()} pri dohvaćanju ciljeva."
                            emptyList()
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "Greška pri dohvaćanju ciljeva: ${e.message}"
                    goals = emptyList()
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
            goals = emptyList()
        }
    }

    fun createGoal(calories: Int, proteins: Double, carbs: Double, fat: Double) {
        val currentUserId = userId
        if (currentUserId == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Morate biti prijavljeni") }
            return
        }
        coroutineScope.launch {
            try {
                val request = GoalCreateDto(
                    calories = calories,
                    targetProteins = proteins,
                    targetCarbs = carbs,
                    targetFats = fat
                )
                val response = RetrofitInstance.api.createGoal(currentUserId, request)
                if (response.isSuccessful) {
                    snackbarHostState.showSnackbar("Cilj uspješno kreiran!")
                    fetchGoals()
                } else {
                    snackbarHostState.showSnackbar("Greška pri kreiranju cilja: ${response.code()}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Greška: ${e.message}")
            } finally {
                showCreateGoalDialog = false
            }
        }
    }

    fun updateGoal(goalId: Int, calories: Int, proteins: Double, carbs: Double, fat: Double) {
        val currentUserId = userId
        if (currentUserId == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Morate biti prijavljeni") }
            return
        }
        coroutineScope.launch {
            try {
                val request = GoalCreateDto(
                    calories = calories,
                    targetProteins = proteins,
                    targetCarbs = carbs,
                    targetFats = fat
                )
                val response = RetrofitInstance.api.updateGoal(goalId, currentUserId, request)
                if (response.isSuccessful) {
                    snackbarHostState.showSnackbar("Cilj uspješno ažuriran!")
                    fetchGoals()
                } else {
                    snackbarHostState.showSnackbar("Greška pri ažuriranju cilja: ${response.code()}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Greška: ${e.message}")
            }
        }
    }

    fun deleteGoal(goal: GoalDto) {
        val currentUserId = userId
        if (currentUserId == null) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Morate biti prijavljeni") }
            return
        }
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.deleteGoal(goal.goalId, currentUserId)
                if (response.isSuccessful) {
                    snackbarHostState.showSnackbar("Cilj uspješno izbrisan!")
                    fetchGoals()
                } else {
                    snackbarHostState.showSnackbar("Greška pri brisanju cilja: ${response.code()}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Greška: ${e.message}")
            }
        }
    }

    LaunchedEffect(userId) { fetchGoals() }

    SmartMenzaTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = BackgroundBeige) {
            Box(modifier = Modifier.fillMaxSize()) {

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
                                text = "Ciljevi",
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
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(0.06f),
                            contentScale = ContentScale.Crop
                        )

                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) { CircularProgressIndicator() }
                            }

                            errorMessage != null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) { Text(text = errorMessage!!, color = Color.Red) }
                            }

                            goals.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Nemate spremljenih ciljeva. Dodajte novi cilj klikom na '+' gumb.")
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        // da zadnji item ne bude ispod FAB-a/snackbara
                                        .padding(bottom = 96.dp)
                                ) {
                                    items(goals) { goal ->
                                        GoalCard(
                                            goal = goal,
                                            onDelete = { goalToDelete = goal },
                                            onEdit = { goalToEdit = goal }
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
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

                FloatingActionButton(
                    onClick = { showCreateGoalDialog = true },
                    containerColor = SpanRed,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj cilj")
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            if (showCreateGoalDialog) {
                CreateGoalDialog(
                    onDismiss = { showCreateGoalDialog = false },
                    onSave = { cal, pro, carb, fat -> createGoal(cal, pro, carb, fat) }
                )
            }

            goalToEdit?.let { goal ->
                EditGoalDialog(
                    goal = goal,
                    onDismiss = { goalToEdit = null },
                    onSave = { cal, pro, carb, fat ->
                        pendingGoalUpdate = PendingGoalUpdate(goal.goalId, cal, pro, carb, fat)
                        goalToEdit = null
                    }
                )
            }

            pendingGoalUpdate?.let { pendingUpdate ->
                EditConfirmationDialog(
                    onConfirm = {
                        updateGoal(
                            goalId = pendingUpdate.goalId,
                            calories = pendingUpdate.calories,
                            proteins = pendingUpdate.proteins,
                            carbs = pendingUpdate.carbs,
                            fat = pendingUpdate.fat
                        )
                        pendingGoalUpdate = null
                    },
                    onDismiss = { pendingGoalUpdate = null }
                )
            }

            goalToDelete?.let { goal ->
                DeleteConfirmationDialog(
                    onConfirm = {
                        deleteGoal(goal)
                        goalToDelete = null
                    },
                    onDismiss = { goalToDelete = null }
                )
            }
        }
    }
}



@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onSave: (calories: Int, proteins: Double, carbs: Double, fat: Double) -> Unit
) {
    var calories by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Postavi novi cilj") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Kalorije (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = proteins, onValueChange = { proteins = it }, label = { Text("Proteini (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Ugljikohidrati (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Masti (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = calories.toIntOrNull()
                val pro = proteins.toDoubleOrNull()
                val car = carbs.toDoubleOrNull()
                val f = fat.toDoubleOrNull()
                if (cal != null && pro != null && car != null && f != null) {
                    onSave(cal, pro, car, f)
                } else {
                    Toast.makeText(context, "Molimo unesite ispravne vrijednosti", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Spremi")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Odustani")
            }
        }
    )
}

@Composable
fun EditGoalDialog(
    goal: GoalDto,
    onDismiss: () -> Unit,
    onSave: (calories: Int, proteins: Double, carbs: Double, fat: Double) -> Unit
) {
    var calories by remember { mutableStateOf(goal.calories.toString()) }
    var proteins by remember { mutableStateOf(goal.protein.toString()) }
    var carbs by remember { mutableStateOf(goal.carbohydrates.toString()) }
    var fat by remember { mutableStateOf(goal.fat.toString()) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uredi cilj") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Kalorije (kcal)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = proteins, onValueChange = { proteins = it }, label = { Text("Proteini (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Ugljikohidrati (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Masti (g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = calories.toIntOrNull()
                val pro = proteins.toDoubleOrNull()
                val car = carbs.toDoubleOrNull()
                val f = fat.toDoubleOrNull()
                if (cal != null && pro != null && car != null && f != null) {
                    onSave(cal, pro, car, f)
                } else {
                    Toast.makeText(context, "Molimo unesite ispravne vrijednosti", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Spremi")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Odustani")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potvrda brisanja") },
        text = { Text("Jeste li sigurni da želite izbrisati ovaj cilj?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SpanRed)
            ) {
                Text("Izbriši")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Odustani")
            }
        }
    )
}

@Composable
fun EditConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potvrda izmjene") },
        text = { Text("Jeste li sigurni da želite spremiti promjene?") },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Spremi")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Odustani")
            }
        }
    )
}
