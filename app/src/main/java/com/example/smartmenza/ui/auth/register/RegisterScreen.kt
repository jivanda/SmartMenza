package com.example.smartmenza.ui.auth.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    Scaffold(topBar = {
        @OptIn(ExperimentalMaterial3Api::class)
        CenterAlignedTopAppBar(title = { Text("Registracija") })
    }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text("Register UI ovdje")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onSuccess) { Text("Kreiraj račun") }
        }
    }
}
