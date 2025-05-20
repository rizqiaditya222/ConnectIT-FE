import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable


import androidx.compose.runtime.Composable

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "onboarding") {
        composable("onboarding") {
            OnboardingScreen(
                onGetStartedClick = {
                    navController.navigate("login")
                }
            )
        }
        composable("login") {
//            LoginScreen(
//                onRegisterClick = {
//                    navController.navigate("register")
//                }
//            )
        }
        composable("register") {
//            RegisterScreen()
        }
    }
}
