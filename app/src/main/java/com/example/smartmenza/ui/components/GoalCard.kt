package com.example.smartmenza.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmenza.data.remote.GoalDto
import com.example.smartmenza.ui.theme.Montserrat

@Composable
fun GoalCard(goal: GoalDto, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cilj: ${goal.dateSet.substringBefore('T')}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Uredi cilj", tint = Color.Gray)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Izbriši cilj", tint = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Kalorije: ${goal.calories} kcal",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Proteini: ${goal.protein} g",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ugljikohidrati: ${goal.carbohydrates} g",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masti: ${goal.fat} g",
                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Montserrat)
            )
        }
    }
}