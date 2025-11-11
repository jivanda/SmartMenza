package com.example.smartmenza.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.smartmenza.R
import com.example.smartmenza.ui.theme.BackgroundBeige
import com.example.smartmenza.ui.theme.Montserrat
import com.example.smartmenza.ui.theme.SpanRed

@Composable
fun HomeScreen(
    ime: String,
    onLogout: () -> Unit = {},
    subtlePattern: Painter = painterResource(id = R.drawable.smartmenza_background_empty)
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundBeige
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // HEADER
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

            // BODY
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = subtlePattern,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.06f),
                    contentScale = ContentScale.Crop
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Naslov s imenom korisnika
                    item {
                        Text(
                            text = "Dobrodošli  $ime 👋",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    // TODO: kasnije dodaj feature kartice (meniji, ciljevi, statistika...)

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Odjava",
                                color = SpanRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onLogout() }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeFeatureCard(title: String, iconRes: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* TODO: Navigate to feature */ },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}