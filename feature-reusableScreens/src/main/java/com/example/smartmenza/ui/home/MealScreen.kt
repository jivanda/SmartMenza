package com.example.smartmenza.ui.home

import MealDto
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.AsyncImage
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.data.remote.FavoriteToggleDto
import com.example.smartmenza.data.remote.RetrofitInstance
import com.example.smartmenza.ui.components.ReviewCard
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import com.example.smartmenza.ui.theme.SpanRed
import kotlinx.coroutines.launch
import com.example.core_ui.R
import com.example.smartmenza.data.remote.RatingCommentDto
import androidx.compose.foundation.lazy.items


data class ReviewUi(
    val rating: Int = 5,
    val comment: String
)

@Composable
fun MealScreen(
    mealId: Int,
    onNavigateBack: () -> Unit,
    onNavigateReview: () -> Unit,
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    val userId by prefs.userId.collectAsState(initial = null)
    val userRole by prefs.userRole.collectAsState(initial = "Student")

    val coroutineScope = rememberCoroutineScope()
    val isStudent = userRole != "Employee"

    var mealDto by remember { mutableStateOf<MealDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var mealTypeName by remember { mutableStateOf<String?>(null) }

    var hasReviewed by remember { mutableStateOf(false) }
    var hasReviews by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<RatingCommentDto>>(emptyList()) }

    var showDeleteReviewDialog by remember { mutableStateOf(false) }

    var averageRating by remember { mutableStateOf<Double?>(null) }
    var ratingsCount by remember { mutableStateOf(0) }

    var favoriteMealIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val isFavorite by remember(favoriteMealIds, mealId) {
        derivedStateOf { favoriteMealIds.contains(mealId) }
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

    fun fetchReviews() {
        coroutineScope.launch {
            try {
                val resp = RetrofitInstance.api.getReviewsByMeal(mealId)
                if (resp.isSuccessful) {
                    val list = resp.body() ?: emptyList()

                    val uid = userId
                    reviews = if (uid != null) {
                        list.sortedWith(
                            compareByDescending<RatingCommentDto> { it.userId == uid }
                        )
                    } else {
                        list
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun fetchRatingSummary() {
        coroutineScope.launch {
            try {
                val resp = RetrofitInstance.api.getReviewSummary(mealId)
                if (resp.isSuccessful) {
                    resp.body()?.let {
                        averageRating = it.averageRating
                        ratingsCount = it.ratingsCount
                    }
                }
            } catch (_: Exception) {
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

    LaunchedEffect(userId, mealId) {
        val uid = userId ?: return@LaunchedEffect
        try {
            val resp = RetrofitInstance.api.hasReviewedMeal(mealId, uid)
            if (resp.isSuccessful) {
                hasReviewed = (resp.body() ?: 0) == 1
            }
        } catch (_: Exception) { }
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

    LaunchedEffect(mealId) {
        fetchReviews()
        fetchRatingSummary()
    }

    fun deleteMyReview() {
        val uid = userId ?: return

        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.deleteRatingComment(
                    mealId = mealId,
                    userId = uid
                )

                if (response.isSuccessful) {
                    Toast.makeText(context, "Recenzija je obrisana.", Toast.LENGTH_SHORT).show()

                    hasReviewed = false
                    fetchReviews()
                    fetchRatingSummary()
                } else {
                    val msg = response.body()?.message
                    Toast.makeText(
                        context,
                        msg ?: "Greška pri brisanju recenzije.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Greška pri brisanju recenzije: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                showDeleteReviewDialog = false
            }
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
                            item {
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                            item {
                                AsyncImage(
                                    model = mealDto!!.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(380.dp)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp
                                            )
                                        ),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mealDto!!.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (isStudent) {
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
                                    if (averageRating != null && ratingsCount > 0) {
                                        hasReviews = true
                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Prosječna ocjena: %.1f / 5  •  %d recenzije"
                                                .format(averageRating, ratingsCount),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Montserrat,
                                                color = SpanRed,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Još nema recenzija",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = Montserrat,
                                                color = Color.Gray
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                    }


                                    if (isStudent) {

                                        if(hasReviewed){
                                            IconButton(onClick = { showDeleteReviewDialog = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Delete,
                                                    contentDescription = "Delete review",
                                                    tint = SpanRed
                                                )
                                            }
                                        }


                                        IconButton(onClick = onNavigateReview) {
                                            Icon(
                                                imageVector = if (!hasReviewed) Icons.Filled.Add else Icons.Filled.Edit,
                                                contentDescription = if (!hasReviewed) "Add review" else "Edit review",
                                                tint = SpanRed
                                            )
                                        }

                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if(hasReviews) {
                                items(reviews) { review ->
                                    ReviewCard(
                                        rating = review.rating,
                                        comment = review.comment ?: "",
                                        username = review.username,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
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

    if (showDeleteReviewDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteReviewDialog = false },
            title = { Text("Brisanje recenzije") },
            text = { Text("Jeste li sigurni da želite obrisati svoju recenziju za ovo jelo?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { deleteMyReview() }
                ) {
                    Text("Obriši", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteReviewDialog = false }
                ) {
                    Text("Odustani")
                }
            }
        )
    }

}