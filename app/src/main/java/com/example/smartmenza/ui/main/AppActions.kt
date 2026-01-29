package com.example.smartmenza.ui.main

import android.net.Uri
import androidx.navigation.NavController
import com.google.gson.Gson

class AppActions(
    private val navController: NavController
) {
    fun navigateToMenu(menuId: Int) {
        navController.navigate("menu/$menuId")
    }


    fun navigateToMeal(mealId: Int) {
        navController.navigate("meal/$mealId")
    }
}

