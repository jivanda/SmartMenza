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
import androidx.compose.ui.res.painterResource
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
import com.example.core_ui.R
import com.example.smartmenza.ui.home.ReviewCreateScreen
import com.example.smartmenza.R as appR
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
                            LoginScreen(
                                webClientId = getString(appR.string.default_web_client_id),
                                subtlePattern = painterResource(id = R.drawable.smartmenza_background_empty),
                                onLoginSuccess = {
                                    navController.navigate(Route.StudentHome.route) {
                                        popUpTo(Route.Login.route) { inclusive = true }
                                    }
                                },
                                onGoToRegister = { navController.navigate(Route.Register.route) }
                            )
                        }

                        composable(Route.Register.route) {
                            RegisterScreen(
                                subtlePattern = painterResource(id = R.drawable.smartmenza_background_empty),
                                onRegistered = {
                                    navController.navigate(Route.Login.route) {
                                        popUpTo(Route.Register.route) { inclusive = true }
                                    }
                                },
                                onNavigateBackToLogin = { navController.popBackStack() }
                            )
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
                                onNavigateToMeal = actions::navigateToMeal
                            )
                        }

                        composable(Route.StatisticsScreen.route) {
                            StatisticsScreen(
                                onNavigateToMeal = actions::navigateToMeal
                            )
                        }

                        composable(Route.Offers.route) {
                            OfferScreen(
                                onNavigateToMenu = actions::navigateToMenu,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(Route.AllMenus.route) {
                            AllMenusScreen(
                                onNavigateToMenu = actions::navigateToMenu,
                                onCreateMenu = { navController.navigate(Route.MenuEditor.route + "/-1") },
                                onEditMenu = { menuId -> navController.navigate(Route.MenuEditor.route + "/$menuId") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Route.ReviewCreate.route,
                            arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getInt("mealId") ?: return@composable

                            ReviewCreateScreen(
                                mealId = mealId,
                                onNavigateBack = { navController.popBackStack() }
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
                                mode = mode,
                                menuTypeOptions = menuTypeOptions,
                                onNavigateBack = { navController.popBackStack() },
                                onSaved = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "meal/{mealId}",
                            arguments = listOf(navArgument("mealId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val mealId = backStackEntry.arguments?.getInt("mealId") ?: return@composable

                            MealScreen(
                                mealId = mealId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateReview = { navController.navigate(Route.ReviewCreate.createRoute(mealId)) }
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
