package com.kotlin.connectit.navigation

import OnboardingScreen // Pastikan import ini benar jika OnboardingScreen ada di root package
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.connectit.data.api.TokenManager
import com.kotlin.connectit.ui.addpost.AddPostScreen
import com.kotlin.connectit.ui.editProfile.EditProfileScreen
import com.kotlin.connectit.ui.editpost.EditPostScreen
import com.kotlin.connectit.ui.gettingstarted.GettingStarted
import com.kotlin.connectit.ui.home.CustomBottomNavBar
import com.kotlin.connectit.ui.home.HomeScreenContent
import com.kotlin.connectit.ui.home.Profile // Pastikan Profile diimport dari package yang benar (ui.profile atau ui.home)
import com.kotlin.connectit.ui.login.LoginScreen
import com.kotlin.connectit.ui.other_user_profile.UserProfileScreen
import com.kotlin.connectit.ui.register.RegisterScreen
import com.kotlin.connectit.ui.search.SearchScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    val token = TokenManager.getToken()
    val isLoggedIn = !token.isNullOrBlank()

    val startDestination = if (isLoggedIn) {
        AppDestinations.MAIN_APP_FLOW
    } else {
        AppDestinations.GETTING_STARTED
    }

    NavHost(navController = navController, startDestination = startDestination) {
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
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.REGISTER) { inclusive = true }
                    }
                },
                onLoginClick = {
                    // Navigasi kembali ke Login, bersihkan stack hingga Register
                    navController.popBackStack(AppDestinations.LOGIN, false)
                    if (navController.currentDestination?.route != AppDestinations.LOGIN) {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.REGISTER) { inclusive = true}
                        }
                    }
                }
            )
        }

        composable(AppDestinations.COMPLETE_PROFILE) {
            EditProfileScreen( // Menggunakan EditProfileScreen untuk complete profile
                onProfileUpdated = {
                    navController.navigate(AppDestinations.MAIN_APP_FLOW) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(AppDestinations.COMPLETE_PROFILE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.MAIN_APP_FLOW) {
            val bottomNavController = rememberNavController()
            Scaffold(
                bottomBar = {
                    CustomBottomNavBar(navController = bottomNavController)
                },
                contentWindowInsets = WindowInsets(0.dp) // Menghilangkan padding default dari status/navigation bar
            ) { paddingValues ->
                NavHost(
                    navController = bottomNavController,
                    startDestination = Screen.Home.route, // Route default untuk tab Home
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Screen.Home.route) {
                        HomeScreenContent(
                            onNavigateToUserProfile = { userId ->
                                navController.navigate(AppDestinations.userProfileDetailRoute(userId))
                            },
                            onNavigateToEditPost = { postId ->
                                navController.navigate(AppDestinations.editPostRoute(postId))
                            }
                        )
                    }
                    composable(Screen.Search.route) {
                        SearchScreen(
                            onNavigateToUserProfile = { userId ->
                                navController.navigate(AppDestinations.userProfileDetailRoute(userId))
                            },
                            onNavigateToPostDetail = { postId ->
                                // navController.navigate(AppDestinations.postDetailRoute(postId)) // Jika ada detail post
                            },
                            onNavigateToEditPost = { postId ->
                                navController.navigate(AppDestinations.editPostRoute(postId))
                            }
                        )
                    }
                    composable(Screen.Profile.route) {
                        Profile(
                            onNavigateToLogin = { // Ini adalah callback yang dipanggil saat logout
                                // TokenManager.clearToken() // Token sudah di-clear di ProfileViewModel
                                navController.navigate(AppDestinations.GETTING_STARTED) { // ✨ Diubah ke GETTING_STARTED
                                    popUpTo(AppDestinations.MAIN_APP_FLOW) { inclusive = true } // Keluar dari main flow
                                    launchSingleTop = true
                                }
                            },
                            onNavigateToEditProfile = { userId ->
                                navController.navigate(AppDestinations.editProfileRoute(userId))
                            },
                            onNavigateToCreatePost = {
                                navController.navigate(AppDestinations.CREATE_POST)
                            },
                            onNavigateToEditPost = { postId ->
                                navController.navigate(AppDestinations.editPostRoute(postId))
                            },
                            onNavigateToUserProfile = { userId ->
                                navController.navigate(AppDestinations.userProfileDetailRoute(userId))
                            }
                        )
                    }
                }
            }
        }

        composable(AppDestinations.USER_PROFILE_DETAIL) { navBackStackEntry ->
            UserProfileScreen(
                navController = navController,
                onNavigateToPostDetail = { postId ->
                    // navController.navigate(AppDestinations.postDetailRoute(postId))
                },
                onUsernameClickInPost = { userId ->
                    navController.navigate(AppDestinations.userProfileDetailRoute(userId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(AppDestinations.POST_DETAIL) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Post Detail Screen for Post ID: $postId", color = Color.White)
                // Implement Post Detail Screen here
            }
        }

        composable(AppDestinations.CREATE_POST) {
            AddPostScreen(
                onPostCreatedSuccessfully = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.EDIT_PROFILE) {
            EditProfileScreen(
                onProfileUpdated = {
                    navController.popBackStack()
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppDestinations.EDIT_POST) {
            EditPostScreen(navController = navController)
        }
    }
}