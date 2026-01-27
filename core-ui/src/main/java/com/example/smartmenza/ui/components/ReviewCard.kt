package com.example.smartmenza.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed
import com.example.core_ui.R

@Composable
fun ReviewCard(
    rating: Int = 5,
    comment: String,
    modifier: Modifier = Modifier,
    imageRes: Int = R.drawable.profile,
) {
    val shape = RoundedCornerShape(16.dp)

    Card(
        shape = shape,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = "User profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Korisnik",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = Montserrat,
                                color = SpanRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Text(
                        text = "$rating/5",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = Montserrat,
                            fontWeight = FontWeight.Bold,
                            color = SpanRed
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
