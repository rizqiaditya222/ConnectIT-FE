package com.kotlin.connectit.navigation

import GettingStarted
import OnboardingScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.ui.home.CustomBottomNavBar
import com.kotlin.connectit.ui.home.HomeScreenContent
import com.kotlin.connectit.ui.home.Profile
import com.kotlin.connectit.ui.home.SearchScreen
import com.kotlin.connectit.ui.login.LoginScreen
import com.kotlin.connectit.ui.profile.CompleteProfileScreen
import com.kotlin.connectit.ui.register.RegisterScreen


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppDestinations.GETTING_STARTED) {

        composable(AppDestinations.GETTING_STARTED) {
            GettingStarted(
                onGetStartedClick = {
                    println("Get started button clicked, navigating to onboarding")
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.GETTING_STARTED) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onBoardingClick = {
                    println("Onboarding finished, navigating to main app flow (navbar)")
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onSuccessfulLogin = {
                    println("Login successful, navigating to main app flow")
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    println("Register link clicked, navigating to register")
                    navController.navigate(AppDestinations.REGISTER)
                }
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onSuccessfulRegister = {
                    println("Register successful, navigating to complete_profile")
                    navController.navigate(AppDestinations.COMPLETE_PROFILE) {
                        popUpTo(AppDestinations.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = {
                    println("Login link clicked, navigating to login")
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onProfileCompleted = {
                    println("Profile completed, navigating to main app flow")
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSkipOrNavigate = {
                    println("Skip complete profile, navigating to main app flow")
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    println("Back clicked from complete profile")
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.MAIN_APP_FLOW) {
            val bottomNavController = rememberNavController() // NavController untuk Bottom Nav

            Scaffold(
                bottomBar = {
                    CustomBottomNavBar(navController = bottomNavController)
                },
                contentWindowInsets = WindowInsets(0.dp) // Atau WindowInsets.systemBars
            ) { paddingValues ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = Screen.Home.route, // Start di tab Home
                    modifier = Modifier.padding(paddingValues) // Gunakan paddingValues dari Scaffold
                ) {
                    composable(Screen.Home.route) {
                        HomeScreenContent() // Gunakan konten Home Screen yang sudah dipisah
                    }
                    composable(Screen.Profile.route) {
                        Profile()
                    }
                    composable(Screen.Search.route) {
                        SearchScreen()
                    }
                }
            }
        }
    }
}