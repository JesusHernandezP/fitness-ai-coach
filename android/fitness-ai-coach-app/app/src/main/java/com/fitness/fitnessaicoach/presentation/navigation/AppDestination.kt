package com.fitness.fitnessaicoach.presentation.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Home : AppDestination("home")
    data object Profile : AppDestination("profile")
    data object BodyMetrics : AppDestination("bodyMetrics")
    data object WeightProgress : AppDestination("weightProgress")
    data object Goals : AppDestination("goals")
    data object DailyLog : AppDestination("dailyLog")
    data object Meal : AppDestination("meal")
    data object Workout : AppDestination("workout")
    data object Coach : AppDestination("coach")
}
