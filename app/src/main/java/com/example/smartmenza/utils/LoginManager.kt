package com.example.smartmenza.utils

import androidx.compose.runtime.Composable

interface LoginManager {
    @Composable
    fun LoginScreen()

    @Composable
    fun OnComposeStart()

    fun onButtonClicked()

    val buttonText: String
}