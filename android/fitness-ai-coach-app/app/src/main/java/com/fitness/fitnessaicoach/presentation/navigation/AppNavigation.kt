package com.fitness.fitnessaicoach.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitness.fitnessaicoach.presentation.auth.LoginScreen
import com.fitness.fitnessaicoach.presentation.bodymetrics.BodyMetricsScreen
import com.fitness.fitnessaicoach.presentation.chat.AIChatScreen
import com.fitness.fitnessaicoach.presentation.bodymetrics.WeightProgressScreen
import com.fitness.fitnessaicoach.presentation.coach.CoachScreen
import com.fitness.fitnessaicoach.presentation.dailylog.DailyLogScreen
import com.fitness.fitnessaicoach.presentation.goals.GoalsScreen
import com.fitness.fitnessaicoach.presentation.home.HomeScreen
import com.fitness.fitnessaicoach.presentation.meal.MealScreen
import com.fitness.fitnessaicoach.presentation.profile.MetabolicProfileScreen
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
                onOpenProfile = {
                    navController.navigate(AppDestination.Profile.route)
                },
                onOpenBodyMetrics = {
                    navController.navigate(AppDestination.BodyMetrics.route)
                },
                onOpenGoals = {
                    navController.navigate(AppDestination.Goals.route)
                },
                onOpenAIChat = {
                    navController.navigate(AppDestination.AIChat.route)
                }
            )
        }
        composable(AppDestination.Profile.route) {
            MetabolicProfileScreen()
        }
        composable(AppDestination.BodyMetrics.route) {
            BodyMetricsScreen(
                onOpenWeightProgress = {
                    navController.navigate(AppDestination.WeightProgress.route)
                }
            )
        }
        composable(AppDestination.WeightProgress.route) {
            WeightProgressScreen()
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
        composable(AppDestination.AIChat.route) {
            AIChatScreen()
        }
    }
}
