package com.farmsos.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.farmsos.domain.model.AuthState
import com.farmsos.ui.auth.AuthViewModel
import com.farmsos.ui.screens.DashboardScreen
import com.farmsos.ui.screens.FarmDetailScreen
import com.farmsos.ui.screens.FlockDetailScreen
import com.farmsos.ui.screens.HomeScreen
import com.farmsos.ui.screens.LoginScreen
import com.farmsos.ui.screens.SettingsScreen
import com.farmsos.ui.screens.ProductionDetailScreen
import com.farmsos.ui.screens.ProductionEditorScreen
import com.farmsos.ui.screens.ProductionHistoryScreen
import com.farmsos.ui.screens.FeedInventoryScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
    data object FarmDetail : Screen("farm/{farmId}") {
        fun create(farmId: String) = "farm/$farmId"
    }
    data object FlockDetail : Screen("farm/{farmId}/flock/{flockId}") {
        fun create(farmId: String, flockId: String) = "farm/$farmId/flock/$flockId"
    }
    data object ProductionHistory : Screen("farm/{farmId}/flock/{flockId}/production")
    data object ProductionNew : Screen("farm/{farmId}/flock/{flockId}/production/new")
    data object ProductionDetail : Screen("farm/{farmId}/flock/{flockId}/production/{productionId}")
    data object ProductionEdit : Screen("farm/{farmId}/flock/{flockId}/production/{productionId}/edit")
    data object FeedInventory : Screen("farm/{farmId}/feed")
}

@Composable
fun FarmOSNavigation(
    navController: NavHostController = rememberNavController()
) {
    val activity = LocalContext.current as ComponentActivity
    val authViewModel: AuthViewModel = hiltViewModel(activity)
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    if (authState is AuthState.Unknown) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when (authState) {
        is AuthState.Authenticated -> Screen.Home.route
        else -> Screen.Login.route
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                val current = navController.currentDestination?.route
                if (current == Screen.Login.route || current == null) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            is AuthState.Unauthenticated -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            AuthState.Unknown -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(viewModel = authViewModel)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(
            route = Screen.FarmDetail.route,
            arguments = listOf(navArgument("farmId") { type = NavType.StringType })
        ) {
            FarmDetailScreen(navController = navController)
        }
        composable(
            route = Screen.FlockDetail.route,
            arguments = listOf(
                navArgument("farmId") { type = NavType.StringType },
                navArgument("flockId") { type = NavType.StringType }
            )
        ) {
            FlockDetailScreen(navController = navController)
        }
        composable(Screen.ProductionHistory.route, arguments = listOf(navArgument("farmId") { type = NavType.StringType }, navArgument("flockId") { type = NavType.StringType })) { ProductionHistoryScreen(navController) }
        composable(Screen.ProductionNew.route, arguments = listOf(navArgument("farmId") { type = NavType.StringType }, navArgument("flockId") { type = NavType.StringType })) { ProductionEditorScreen(navController) }
        composable(Screen.ProductionDetail.route, arguments = listOf(navArgument("farmId") { type = NavType.StringType }, navArgument("flockId") { type = NavType.StringType }, navArgument("productionId") { type = NavType.StringType })) { ProductionDetailScreen(navController) }
        composable(Screen.ProductionEdit.route, arguments = listOf(navArgument("farmId") { type = NavType.StringType }, navArgument("flockId") { type = NavType.StringType }, navArgument("productionId") { type = NavType.StringType })) { ProductionEditorScreen(navController) }
        composable(Screen.FeedInventory.route, arguments = listOf(navArgument("farmId") { type = NavType.StringType })) { FeedInventoryScreen(navController) }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
    }
}
