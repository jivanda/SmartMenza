package com.example.smartmenza.ui.main

import android.net.Uri
import androidx.navigation.NavController
import com.google.gson.Gson

class AppActions(
    private val navController: NavController
) {
    fun navigateToMenu(menuName: String, mealsJson: String) {
        val encodedName = Uri.encode(menuName)
        val encodedMeals = Uri.encode(mealsJson)
        navController.navigate("menu/$encodedName/$encodedMeals")
    }

    fun navigateToMeal(mealId: Int) {
        navController.navigate("meal/$mealId")
    }
}

