package com.kotlin.connectit.navigation // Ganti dengan package navigasi Anda

import GettingStarted
import OnboardingScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.ui.login.LoginScreen
import com.kotlin.connectit.ui.profile.CompleteProfileScreen
import com.kotlin.connectit.ui.register.RegisterScreen

object AppDestinations {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val COMPLETE_PROFILE = "complete_profile"
    const val GETTING_STARTED = "getting_started"
    const val HOME = "home"
}

@Composable
fun HomeScreen(navController: NavHostController) { // Tambahkan NavController jika perlu navigasi dari home
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to Home Screen!")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                TokenManager.clearToken() // Hapus token
                navController.navigate(AppDestinations.LOGIN) {
                    popUpTo(navController.graph.findStartDestination().id) { // popUpTo start destination of graph
                        inclusive = true
                    }
                    launchSingleTop = true // Hindari multiple copies of login screen
                }
            }) {
                Text("Logout")
            }
        }
    }
}


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AppDestinations.ONBOARDING) {

        composable(AppDestinations.ONBOARDING) {
            OnboardingScreen(
                onBoardingClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onSuccessfulLogin = {
                    println("Login successful, navigating to home")
                    navController.navigate(AppDestinations.HOME) {
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
                // Menggunakan callback onSuccessfulRegister yang baru
                onSuccessfulRegister = {
                    println("Register successful, navigating to complete_profile")
                    navController.navigate(AppDestinations.COMPLETE_PROFILE) {
                        // Anda mungkin ingin popUpTo register agar tidak bisa kembali ke register
                        // setelah melengkapi profil, atau biarkan untuk alur "kembali" dari complete profile
                        popUpTo(AppDestinations.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = {
                    println("Login link clicked, navigating to login")
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true } // Kembali ke login, jangan buat instance baru
                    }
                }
            )
        }

        composable(AppDestinations.COMPLETE_PROFILE) {
            CompleteProfileScreen(
                onProfileCompleted = {
                    println("Profile completed, navigating to home")
                    navController.navigate(AppDestinations.HOME) {
                        // Setelah profil selesai, hapus Register dan CompleteProfile dari backstack
                        popUpTo(navController.graph.findStartDestination().id) { // Kembali ke root awal graph
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSkipOrNavigate = {
                    println("Skip complete profile, navigating to home")
                    navController.navigate(AppDestinations.HOME) {
                        // Sama seperti di atas, anggap skip juga final
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    println("Back clicked from complete profile")
                    navController.popBackStack() // Kembali ke layar sebelumnya (Register)
                }
            )
        }

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

        composable(AppDestinations.HOME) {
            HomeScreen(navController)
        }
    }
}