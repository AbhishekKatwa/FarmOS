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
import com.farmsos.domain.model.AuthState
import com.farmsos.ui.auth.AuthViewModel
import com.farmsos.ui.screens.DashboardScreen
import com.farmsos.ui.screens.HomeScreen
import com.farmsos.ui.screens.LoginScreen
import com.farmsos.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Settings : Screen("settings")
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
