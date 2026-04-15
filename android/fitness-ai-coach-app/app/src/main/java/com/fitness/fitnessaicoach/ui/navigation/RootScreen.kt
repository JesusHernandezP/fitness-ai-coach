package com.fitness.fitnessaicoach.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.BackgroundBase
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.TextSecondary

sealed class RootDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : RootDestination("dashboard", "Panel", Icons.Default.Home)
    data object Chat : RootDestination("chat", "Chat", Icons.Default.Email)
    data object Profile : RootDestination("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun RootScreen() {
    val navController = rememberNavController()
    var selectedDestinationIndex by rememberSaveable { mutableIntStateOf(0) }

    val rootDestinations = listOf(
        RootDestination.Dashboard,
        RootDestination.Chat,
        RootDestination.Profile
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = CardSurface,
                tonalElevation = 0.dp
            ) {
                rootDestinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selectedDestinationIndex == index,
                        onClick = {
                            selectedDestinationIndex = index
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = { Text(destination.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentYellow,
                            selectedTextColor = AccentYellow,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = BackgroundBase
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        RootNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
