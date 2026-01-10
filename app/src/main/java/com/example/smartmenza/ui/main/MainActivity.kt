package com.example.smartmenza.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.smartmenza.ui.features.StatisticsScreen
import com.example.smartmenza.ui.home.FavouriteScreen
import com.example.smartmenza.ui.home.GoalScreen
import com.example.smartmenza.ui.home.HomeScreen
import com.example.smartmenza.ui.home.MealScreen
import com.example.smartmenza.ui.home.MenuScreen
import com.example.smartmenza.ui.intro.IntroScreen
import com.example.smartmenza.ui.theme.SmartMenzaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartMenzaTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()
                    val actions = remember(navController) { AppActions(navController) }

                    val prefs = remember { UserPreferences(this) }
                    val isLoggedIn by prefs.isLoggedIn.collectAsState(initial = false)

                    val scope = rememberCoroutineScope()

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
                            RegisterScreen(navController = navController)
                        }

                        composable(Route.StudentHome.route) {
                            HomeScreen(
                                onNavigateToFavorites = { navController.navigate(Route.Favourite.route) },
                                onNavigateToGoals = { navController.navigate(Route.Goal.route) },
                                onNavigateToMenu = actions::navigateToMenu,
                                onLogout = {
                                    scope.launch { prefs.logout() }
                                    navController.navigate(Route.Login.route) {
                                        popUpTo(Route.StudentHome.route) { inclusive = true }
                                    }
                                },
                                onAllMeals = { navController.navigate(Route.AllMeals.route) },
                                onOffers = { navController.navigate(Route.Offers.route) },
                                onAllMenus = { navController.navigate(Route.AllMenus.route) },
                                onNavigateToStatistics = { navController.navigate(Route.StatisticsScreen.route) }
                            )
                        }

                        composable(Route.AllMeals.route) {
                            AllMealsScreen(
                                navController = navController,
                                onNavigateToMeal = actions::navigateToMeal,
                            )
                        }

                        composable(Route.StatisticsScreen.route) {
                            StatisticsScreen(
                                navController = navController,
                                onNavigateToMeal = actions::navigateToMeal,
                            )
                        }

                        composable(Route.Offers.route) {
                            OfferScreen(
                                onNavigateToMenu = actions::navigateToMenu,
                                navController = navController)
                        }

                        composable(Route.AllMenus.route) {
                            AllMenusScreen(
                                onNavigateToMenu = actions::navigateToMenu,
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
                            val mode = if (menuId == -1) MenuEditMode.Create else MenuEditMode.Edit(menuId)

                            val menuTypeOptions = listOf(
                                MenuTypeOption(1, "Doručak"),
                                MenuTypeOption(2, "Ručak"),
                                MenuTypeOption(3, "Večera")
                            )

                            MenuEditScreen(
                                navController = navController,
                                mode = mode,
                                menuTypeOptions = menuTypeOptions
                            )
                        }

                        composable(
                            route = "meal/{mealId}",
                            arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getInt("mealId") ?: return@composable

                            MealScreen(
                                mealId = mealId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Route.Favourite.route) {
                            FavouriteScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable(Route.Goal.route) {
                            GoalScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        // IMPORTANT: This must match your Route.Menu.route pattern AND navArguments
                        composable(
                            route = Route.Menu.route,
                            arguments = listOf(
                                navArgument("menuName") { type = NavType.StringType },
                                navArgument("mealsJson") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val menuName = backStackEntry.arguments?.getString("menuName").orEmpty()
                            val mealsJson = backStackEntry.arguments?.getString("mealsJson").orEmpty()

                            MenuScreen(
                                menuName = menuName,
                                mealsJson = mealsJson,
                                onNavigateToMeal = actions::navigateToMeal,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
