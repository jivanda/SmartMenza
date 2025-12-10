package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.smartmenza.R
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

@Composable
fun GoalScreen(
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    SmartMenzaTheme {
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var goals by remember { mutableStateOf<List<GoalDto>>(emptyList()) }
        var showCreateGoalDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val prefs = remember { UserPreferences(context) }
        val userId by prefs.userId.collectAsState(initial = null)

        fun fetchGoals() {
            val currentUserId = userId
            if (currentUserId != null) {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val response = RetrofitInstance.api.getMyGoals(currentUserId)
                        if (response.isSuccessful) {
                            goals = response.body() ?: emptyList()
                        } else if (response.code() == 404) {
                            goals = emptyList()
                        } else {
                            errorMessage = "Greška ${response.code()} pri dohvaćanju ciljeva."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Greška pri dohvaćanju ciljeva: ${e.message}"
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
                Toast.makeText(context, "Morate biti prijavljeni", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "Cilj uspješno kreiran!", Toast.LENGTH_SHORT).show()
                        fetchGoals()
                        showCreateGoalDialog = false
                    } else {
                        Toast.makeText(context, "Greška pri kreiranju cilja: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun deleteGoal(goalId: Int) {
            val currentUserId = userId
            if (currentUserId == null) {
                Toast.makeText(context, "Morate biti prijavljeni", Toast.LENGTH_SHORT).show()
                return
            }
            coroutineScope.launch {
                try {
                    val response = RetrofitInstance.api.deleteGoal(goalId, currentUserId)
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Cilj uspješno izbrisan!", Toast.LENGTH_SHORT).show()
                        fetchGoals() // Refresh the list
                    } else {
                        Toast.makeText(context, "Greška pri brisanju cilja: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        LaunchedEffect(userId) {
            fetchGoals()
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateGoalDialog = true },
                    containerColor = SpanRed,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj cilj")
                }
            },
            containerColor = BackgroundBeige
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                        Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
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

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = errorMessage!!, color = Color.Red)
                        }
                    } else if (goals.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nemate spremljenih ciljeva. Dodajte novi cilj klikom na '+' gumb.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            items(goals) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onDelete = { deleteGoal(goal.goalId) }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        if (showCreateGoalDialog) {
            CreateGoalDialog(
                onDismiss = { showCreateGoalDialog = false },
                onSave = { cal, pro, carb, fat ->
                    createGoal(cal, pro, carb, fat)
                }
            )
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
