package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.smartmenza.data.remote.MealDto
import com.example.smartmenza.data.remote.RatingCommentCreateDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import kotlinx.coroutines.launch

@Composable
fun ReviewCreateScreen(
    mealId: Int,
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    val ctx = LocalContext.current

    val prefs = remember { UserPreferences(ctx) }
    val userNameNullable by prefs.userName.collectAsState(initial = null)
    val userName = userNameNullable ?: "Korisnik"


    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    var mealDto by remember { mutableStateOf<MealDto?>(null) }

    val userId by prefs.userId.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()

    fun submitReview() {
        val uid = userId
        if (uid == null) {
            Toast.makeText(ctx, "Morate biti prijavljeni", Toast.LENGTH_SHORT).show()
            return
        }
        if (rating !in 1..5) {
            Toast.makeText(ctx, "Odaberite ocjenu (1-5).", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.createRatingComment(
                    userId = uid,
                    dto = RatingCommentCreateDto(
                        mealId = mealId,
                        rating = rating,
                        comment = comment.trim().ifBlank { null }
                    )
                )

                if (response.isSuccessful) {
                    Toast.makeText(ctx, "Recenzija je spremljena.", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                } else {
                    val msg = response.body()?.message
                    Toast.makeText(
                        ctx,
                        msg ?: "Greška: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    LaunchedEffect(mealId) {
        try {
            val response = RetrofitInstance.api.getMealById(mealId)
            if (response.isSuccessful) {
                mealDto = response.body()
            }
        } catch (_: Exception) { }
    }


    SmartMenzaTheme {
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
                            text = "Recenzija",
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
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.06f),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.hrenovke),
                            contentDescription = mealDto?.name ?: "Meal image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = mealDto?.name ?: "Jelo",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold,
                                color = SpanRed
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.profile),
                                    contentDescription = "User profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = SpanRed
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = "Star $i",
                                        tint = if (i <= rating) Color(0xFFFFC107) else Color.Gray,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clickable { rating = i }
                                            .padding(2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "$rating/5",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = SpanRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = comment,
                            onValueChange = { comment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            placeholder = { Text("Upišite komentar...") },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Montserrat),
                            maxLines = 10,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { submitReview()},
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SpanRed),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Submit",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }

}

