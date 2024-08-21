package idat.edu.quesosexpress.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import idat.edu.quesosexpress.ui.navigation.Screen

@Composable
fun MenuDrawer(navController: NavController, onCloseDrawer: () -> Unit) {
    Column(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .fillMaxHeight()
            .background(Color.White),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Quesos Express",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable {
                    navController.navigate(Screen.Home.route)
                    onCloseDrawer()
                }
        )

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        MenuItem(text = "Carrito de compras") {
            navController.navigate(Screen.Cart.route)
            onCloseDrawer()
        }
        MenuItem(text = "Perfil de usuario") {
            navController.navigate(Screen.Profile.route)
            onCloseDrawer()
        }
        MenuItem(text = "Historial de pedidos") {
            navController.navigate(Screen.OrderHistory.route)
            onCloseDrawer()
        }
        MenuItem(text = "Cerrar sesión") {
            // Cerrar sesión y forzar la selección de cuenta
            val googleSignInClient = GoogleSignIn.getClient(navController.context, GoogleSignInOptions.DEFAULT_SIGN_IN)
            googleSignInClient.signOut().addOnCompleteListener {
                Firebase.auth.signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
                onCloseDrawer()
            }
        }
    }
}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
    Divider()
}