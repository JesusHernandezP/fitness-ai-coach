package com.fitness.fitnessaicoach.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitness.fitnessaicoach.presentation.auth.LoginScreen
import com.fitness.fitnessaicoach.presentation.auth.RegisterScreen
import com.fitness.fitnessaicoach.ui.navigation.RootScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route
    ) {
        composable(AppDestination.Login.route) { backStackEntry ->
            val successMessage = backStackEntry.savedStateHandle.get<String>("successMessage")
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                },
                onCreateAccount = {
                    navController.navigate(AppDestination.Register.route)
                },
                successMessage = successMessage
            )
        }
        composable(AppDestination.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("successMessage", "Cuenta creada. Inicia sesion.")
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppDestination.Home.route) {
            RootScreen()
        }
    }
}
