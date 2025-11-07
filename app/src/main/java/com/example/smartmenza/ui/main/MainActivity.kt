package com.example.smartmenza.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartmenza.navigation.Route
import com.example.smartmenza.ui.auth.login.LoginScreen
import com.example.smartmenza.ui.auth.register.RegisterScreen
import com.example.smartmenza.ui.intro.IntroScreen
import com.example.smartmenza.ui.theme.SmartMenzaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartMenzaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val nav = rememberNavController()
                    NavHost(navController = nav, startDestination = Route.Intro.route) {
                        composable(Route.Intro.route) {
                            IntroScreen(
                                onLogin = { nav.navigate(Route.Login.route) },
                                onRegister = { nav.navigate(Route.Register.route) }
                            )
                        }
                        composable(Route.Login.route) {
                            LoginScreen(
                                onBack = { nav.popBackStack() },
                                onSuccess = { /* nav.navigate(Route.StudentHome.route) { popUpTo(Route.Intro.route) { inclusive = true } } */ }
                            )
                        }
                        composable(Route.Register.route) {
                            RegisterScreen(
                                onBack = { nav.popBackStack() },
                                onSuccess = { /* nav.navigate(Route.Login.route) */ }
                            )
                        }
                    }
                }
            }
        }
    }
}