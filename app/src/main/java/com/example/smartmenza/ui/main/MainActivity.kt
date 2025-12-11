package com.example.smartmenza.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartmenza.data.local.UserPreferences
import com.example.smartmenza.navigation.Route
import com.example.smartmenza.ui.auth.login.LoginScreen
import com.example.smartmenza.ui.auth.register.RegisterScreen
import com.example.smartmenza.ui.features.AllMealsScreen
import com.example.smartmenza.ui.features.AllMenusScreen
import com.example.smartmenza.ui.features.MenuEditMode
import com.example.smartmenza.ui.features.MenuEditScreen
import com.example.smartmenza.ui.features.MenuTypeOption
import com.example.smartmenza.ui.features.OfferScreen
import com.example.smartmenza.ui.home.FavouriteScreen
import com.example.smartmenza.ui.home.GoalScreen
import com.example.smartmenza.ui.home.HomeScreen
import com.example.smartmenza.ui.home.MenuScreen
import com.example.smartmenza.ui.intro.IntroScreen
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartMenzaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val prefs = UserPreferences(this)
                    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)

                    NavHost(
                        navController = navController,
                        startDestination = if (isLoggedIn) Route.StudentHome.route else Route.Intro.route
                    ) {
                        composable(Route.Intro.route) {
                            IntroScreen(
                                onLogin = { navController.navigate(Route.Login.route) },
                                onRegister = { navController.navigate(Route.Register.route) }
                            )
                        }

                        composable(Route.Login.route) {
                            LoginScreen(navController = navController)
                        }

                        composable(Route.Register.route) {
                            RegisterScreen(
                                navController = navController
                            )
                        }

                        composable(Route.StudentHome.route) {
                            HomeScreen(
                                onNavigateToFavorites = { navController.navigate(Route.Favourite.route) },
                                onNavigateToGoals = { navController.navigate(Route.Goal.route) },
                                onNavigateToMenu = { menuName, mealsJson ->
                                    navController.navigate("menu/$menuName/$mealsJson")
                                },
                                onLogout = {
                                    runBlocking {
                                        prefs.logout()
                                    }
                                    navController.navigate(Route.Login.route) {
                                        popUpTo(Route.StudentHome.route) { inclusive = true }
                                    }
                                },
                                onAllMeals = { navController.navigate(Route.AllMeals.route) },
                                onOffers = { navController.navigate(Route.Offers.route) },
                                onAllMenus = { navController.navigate(Route.AllMenus.route) }
                            )
                        }

                        composable(Route.AllMeals.route) {
                            AllMealsScreen(
                                navController = navController
                            )
                        }

                        composable(Route.Offers.route) {
                            OfferScreen(
                                navController = navController
                            )
                        }

                        composable(Route.AllMenus.route) {
                            AllMenusScreen(
                                navController = navController
                            )
                        }

                        composable(
                            route = Route.MenuEditor.route + "/{menuId}",
                            arguments = listOf(
                                navArgument("menuId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val menuId = backStackEntry.arguments?.getInt("menuId") ?: -1

                            val mode = if (menuId == -1) {
                                MenuEditMode.Create
                            } else {
                                MenuEditMode.Edit(menuId)
                            }

                            val menuTypeOptions = listOf(
                                MenuTypeOption(1, "Doručak"),
                                MenuTypeOption(2, "Ručak"),
                                MenuTypeOption(3, "Večera")
                            )

                            MenuEditScreen(
                                navController = navController,
                                mode = mode,
                                menuTypeOptions = menuTypeOptions
                                // for edit mode, later you can pass initialName, initialDescription, etc.
                            )
                        }

                        composable(Route.Favourite.route) {
                            FavouriteScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(Route.Goal.route) {
                            GoalScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(Route.Menu.route) { backStackEntry ->
                            val menuName = backStackEntry.arguments?.getString("menuName") ?: ""
                            val mealsJson = backStackEntry.arguments?.getString("mealsJson") ?: ""
                            MenuScreen(
                                menuName = menuName,
                                mealsJson = mealsJson,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
