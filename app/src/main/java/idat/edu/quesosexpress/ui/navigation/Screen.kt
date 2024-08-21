package idat.edu.quesosexpress.ui.navigation
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import idat.edu.quesosexpress.ui.screens.CartScreen
import idat.edu.quesosexpress.ui.screens.CartViewModel
import idat.edu.quesosexpress.ui.screens.HomeScreen
import idat.edu.quesosexpress.ui.screens.LoginScreen
import idat.edu.quesosexpress.ui.screens.OrderHistoryScreen
import idat.edu.quesosexpress.ui.screens.PaymentScreen
import idat.edu.quesosexpress.ui.screens.ProfileScreen
import idat.edu.quesosexpress.ui.utils.PreferenceManager

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Cart : Screen("cart")
    object Payment : Screen("payment")
    object OrderHistory : Screen("order_history")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    signInLauncher: ActivityResultLauncher<Intent>,
    signInIntent: Intent,
    modifier: Modifier = Modifier,
    onLoginScreen: (Boolean) -> Unit,
    cartViewModel: CartViewModel,
    preferenceManager: PreferenceManager
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            onLoginScreen(true)
            LoginScreen(navController, signInLauncher, signInIntent)
        }
        composable(Screen.Profile.route) {
            onLoginScreen(false)
            ProfileScreen(navController)
        }
        composable(Screen.Home.route) {
            onLoginScreen(false)
            HomeScreen(navController, cartViewModel)
        }
        composable(Screen.Cart.route) {
            onLoginScreen(false)
            CartScreen(navController, cartViewModel, preferenceManager)
        }
        composable(Screen.Payment.route) {
            onLoginScreen(false)
            PaymentScreen(navController, cartViewModel)  // <-- Pasando cartViewModel aquí
        }
        composable(Screen.OrderHistory.route) {
            onLoginScreen(false)
            OrderHistoryScreen(navController, cartViewModel)  // <-- Pasando cartViewModel aquí
        }
    }
}