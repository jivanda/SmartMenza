package com.example.smartmenza.navigation

sealed interface Route { val route: String
    data object Intro : Route { override val route = "intro" }
    data object Login : Route { override val route = "login" }
    data object Register : Route { override val route = "register" }
    // kasnije
    data object StudentHome : Route { override val route = "student/home" }
    data object StaffHome : Route { override val route = "staff/home" }
}
