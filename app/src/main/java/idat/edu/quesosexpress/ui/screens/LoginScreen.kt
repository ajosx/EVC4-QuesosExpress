package idat.edu.quesosexpress.ui.screens
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import idat.edu.quesosexpress.R

@Composable
fun LoginScreen(
    navController: NavController,
    signInLauncher: ActivityResultLauncher<Intent>,
    signInIntent: Intent
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Quesos Express",
            modifier = Modifier
                .size(120.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Bienvenido a la app de\npedidos de Quesos Express")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            signInLauncher.launch(signInIntent)
        }) {
            Text(text = "Comenzar pedido")
        }
    }
}