
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "onboarding") {

        composable("onboarding") {
            OnboardingScreen(
                onBoardingClick = {
                    navController.navigate("login") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginClick = {
                    println("Login button clicked, navigating to getting_started")
                    navController.navigate("getting_started")
                },
                onRegisterClick = {
                    println("Register link clicked, navigating to register")
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterClick = {
                    println("Register button clicked, navigating to login")
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = {
                    println("Login link clicked, going back to login")
                    navController.navigate("login")
                }
            )
        }

        composable("getting_started") {
            GettingStarted(
                onGetStartedClick = {
                    println("Get started button clicked, navigating to onboarding")
                    navController.navigate("onboarding") {
                        popUpTo("getting_started") { inclusive = true }
                    }
                }
            )
        }
    }
}