package com.fitness.fitnessaicoach.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitness.fitnessaicoach.presentation.chat.AIChatScreen
import com.fitness.fitnessaicoach.presentation.home.HomeScreen
import com.fitness.fitnessaicoach.presentation.profile.MetabolicProfileScreen

@Composable
fun RootNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = RootDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(RootDestination.Dashboard.route) {
            HomeScreen(
                onOpenProfile = {
                    navController.navigate(RootDestination.Profile.route)
                },
                onOpenBodyMetrics = { },
                onOpenGoals = { },
                onOpenAIChat = {
                    navController.navigate(RootDestination.Chat.route)
                }
            )
        }

        composable(RootDestination.Chat.route) {
            AIChatScreen()
        }

        composable(RootDestination.Profile.route) {
            MetabolicProfileScreen()
        }
    }
}
