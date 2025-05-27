package com.kotlin.connectit.navigation

import GettingStarted
import OnboardingScreen
import android.window.SplashScreen
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
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
import com.kotlin.connectit.ui.login.LoginScreen
import com.kotlin.connectit.ui.profile.CompleteProfileScreen
import com.kotlin.connectit.ui.register.RegisterScreen
import com.kotlin.connectit.ui.search.SearchScreen

@Composable
fun HomeScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to Home Screen!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                TokenManager.clearToken()
                navController.navigate(AppDestinations.LOGIN) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppDestinations.GETTING_STARTED) {

        composable(AppDestinations.GETTING_STARTED) {
            GettingStarted(
                onGetStartedClick = {
                    navController.navigate(AppDestinations.ONBOARDING) {
                        popUpTo(AppDestinations.GETTING_STARTED) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onBoardingClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onSuccessfulLogin = {
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(AppDestinations.REGISTER)
                }
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onSuccessfulRegister = {
                    navController.navigate(AppDestinations.COMPLETE_PROFILE) {
                        popUpTo(AppDestinations.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onProfileCompleted = {
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSkipOrNavigate = {
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.MAIN_APP_FLOW) {
            val bottomNavController = rememberNavController()
            Scaffold(
                bottomBar = {
                    CustomBottomNavBar(navController = bottomNavController)
                },
                contentWindowInsets = WindowInsets(0.dp)
            ) { paddingValues ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Screen.Home.route) { HomeScreenContent() }
                    composable(Screen.Search.route) { SearchScreen(
                        onNavigateToUserProfile = {},
                        onNavigateToPostDetail = {}
                    ) }
                    composable(Screen.Profile.route) { Profile(
                        onNavigateToCreatePost = {},
                        onNavigateToLogin = {},
                        onNavigateToEditProfile = {}
                    ) }
                }
            }
        }
    }
}