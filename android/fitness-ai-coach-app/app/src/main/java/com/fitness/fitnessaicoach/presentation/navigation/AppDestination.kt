package com.fitness.fitnessaicoach.presentation.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Home : AppDestination("home")
    data object DailyLog : AppDestination("dailyLog")
    data object Meal : AppDestination("meal")
    data object Workout : AppDestination("workout")
    data object Coach : AppDestination("coach")
}
