package com.example.smartmenza.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.R
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.GoalDto
import com.example.smartmenza.data.remote.RetrofitInstance
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

        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current
        val prefs = remember { UserPreferences(context) }
        val userId by prefs.userId.collectAsState(initial = null)

        LaunchedEffect(userId) {
            val currentUserId = userId
            if (currentUserId != null) {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val response = RetrofitInstance.api.getMyGoals(currentUserId)

                        if (response.isSuccessful) {
                            goals = response.body() ?: emptyList()
                        } else if (response.code() == 404) {
                            goals = emptyList() // Treat 404 as no goals
                        } else {
                            errorMessage = "Greška ${'$'}{response.code()} pri dohvaćanju ciljeva."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Greška pri dohvaćanju ciljeva: ${'$'}{e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            } else {
                isLoading = false
                goals = emptyList()
            }
        }

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

                    // Background pattern
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nemate spremljenih ciljeva.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            items(goals) { goal ->
                                GoalCard(goal = goal)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalCard(goal: GoalDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cilj postavljen: ${'$'}{goal.dateSet.substringBefore('T')}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kalorije: ${'$'}{goal.calories} kcal",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Proteini: ${'$'}{goal.protein} g",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ugljikohidrati: ${'$'}{goal.carbohydrates} g",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masti: ${'$'}{goal.fat} g",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Montserrat
                )
            )
        }
    }
}
