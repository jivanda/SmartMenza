package com.example.smartmenza.ui.intro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(onLogin: () -> Unit, onRegister: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Dobrodošao u SmartMenza", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onLogin) { Text("Prijava") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onRegister) { Text("Registracija") }
        }
    }
}
