package com.example.smartmenza.navigation

sealed interface Route { val route: String
    data object Intro : Route { override val route = "intro" }
    data object Login : Route { override val route = "login" }
    data object Register : Route { override val route = "register" }
    data object Favourite : Route { override val route = "favourite" }
    data object Goal : Route { override val route = "goal" }
    data object Menu : Route { override val route = "menu/{menuName}/{mealsJson}" }
    data object AllMeals : Route { override val route = "features/all_meals" }
    data object Offers : Route { override val route = "features/offers" }
    data object AllMenus : Route { override val route = "features/all_menus" }
    // kasnije
    data object StudentHome : Route { override val route = "student/home" }
    //data object StaffHome : Route { override val route = "staff/home" }
}
