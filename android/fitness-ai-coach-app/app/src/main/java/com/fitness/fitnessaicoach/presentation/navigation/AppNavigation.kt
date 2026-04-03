package com.fitness.fitnessaicoach.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitness.fitnessaicoach.presentation.auth.LoginScreen
import com.fitness.fitnessaicoach.presentation.bodymetrics.BodyMetricsScreen
import com.fitness.fitnessaicoach.presentation.coach.CoachScreen
import com.fitness.fitnessaicoach.presentation.dailylog.DailyLogScreen
import com.fitness.fitnessaicoach.presentation.goals.GoalsScreen
import com.fitness.fitnessaicoach.presentation.home.HomeScreen
import com.fitness.fitnessaicoach.presentation.meal.MealScreen
import com.fitness.fitnessaicoach.presentation.workout.WorkoutScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(AppDestination.Home.route) {
            HomeScreen(
                onOpenBodyMetrics = {
                    navController.navigate(AppDestination.BodyMetrics.route)
                },
                onOpenGoals = {
                    navController.navigate(AppDestination.Goals.route)
                }
            )
        }
        composable(AppDestination.BodyMetrics.route) {
            BodyMetricsScreen()
        }
        composable(AppDestination.Goals.route) {
            GoalsScreen()
        }
        composable(AppDestination.DailyLog.route) {
            DailyLogScreen()
        }
        composable(AppDestination.Meal.route) {
            MealScreen()
        }
        composable(AppDestination.Workout.route) {
            WorkoutScreen()
        }
        composable(AppDestination.Coach.route) {
            CoachScreen()
        }
    }
}
