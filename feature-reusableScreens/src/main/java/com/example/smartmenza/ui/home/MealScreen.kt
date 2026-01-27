package com.example.smartmenza.ui.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.MealDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.ReviewCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import kotlinx.coroutines.launch
import com.example.core_ui.R

data class ReviewUi(
    val rating: Int = 5,
    val comment: String
)

@Composable
fun MealScreen(
    mealId: Int,
    onNavigateBack: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    val userId by prefs.userId.collectAsState(initial = null)
    val userRole by prefs.userRole.collectAsState(initial = "Student")

    val coroutineScope = rememberCoroutineScope()
    val shouldShowFavorite = userRole != "Employee"

    var mealDto by remember { mutableStateOf<MealDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mealTypeName by remember { mutableStateOf<String?>(null) }

    var favoriteMealIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val isFavorite by remember(favoriteMealIds, mealId) {
        derivedStateOf { favoriteMealIds.contains(mealId) }
    }

    val reviews = remember {
        listOf(
            ReviewUi(5, "Odlično jelo, jako ukusno i svježe. Porcija je super i cijena korektna."),
            ReviewUi(4, "Dobro, ali bi moglo biti malo više začina. Inače sve ok."),
            ReviewUi(5, "Top! Uzeo bih opet. Posebno mi se svidio prilog.")
        )
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
                    favoriteMealIds = if (isCurrentlyFavorite) {
                        favoriteMealIds - mealId
                    } else {
                        favoriteMealIds + mealId
                    }
                } else {
                    Toast.makeText(context, "Greška: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Greška: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(mealId) {
        try {
            val response = RetrofitInstance.api.getMealById(mealId)
            if (response.isSuccessful) {
                mealDto = response.body()
            } else {
                error = "Greška: ${response.code()}"
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(userId) {
        fetchFavorites()
    }

    LaunchedEffect(mealDto) {
        val typeId = mealDto?.mealTypeId ?: return@LaunchedEffect

        try {
            val response = RetrofitInstance.api.getMealTypeName(typeId)
            if (response.isSuccessful) {
                mealTypeName = response.body()
            } else {
                mealTypeName = "—"
            }
        } catch (e: Exception) {
            mealTypeName = "—"
        }
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
                            text = mealDto?.name ?: "",
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

                    if (mealDto != null) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            item {
                                Image(
                                    painter = painterResource(id = R.drawable.hrenovke),
                                    contentDescription = mealDto!!.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mealDto!!.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (shouldShowFavorite) {
                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(onClick = { toggleFavorite(mealId) }) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                                                contentDescription = "Favorite",
                                                tint = if (isFavorite) Color.Yellow else Color.Gray
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                mealDto!!.description?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Text("Cijena: %.2f EUR".format(mealDto!!.price))
                                Text("Tip jela: ${mealTypeName}")
                                Text("Kalorije: ${mealDto!!.calories ?: "—"} kcal")
                                Text("Proteini: ${mealDto!!.protein ?: "—"} g")
                                Text("Ugljikohidrati: ${mealDto!!.carbohydrates ?: "—"} g")
                                Text("Masti: ${mealDto!!.fat ?: "—"} g")

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Recenzije",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = Montserrat,
                                            fontWeight = FontWeight.Bold,
                                            color = SpanRed
                                        )
                                    )

                                    IconButton(onClick = { }) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Add review",
                                            tint = SpanRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            items(reviews) { review ->
                                ReviewCard(
                                    rating = review.rating,
                                    comment = review.comment,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (error != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = error ?: "Greška",
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}
