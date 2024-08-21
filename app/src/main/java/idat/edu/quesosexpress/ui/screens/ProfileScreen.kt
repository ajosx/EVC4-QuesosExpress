package idat.edu.quesosexpress.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import idat.edu.quesosexpress.ui.navigation.Screen

@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val db = Firebase.firestore

    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    val correo = user?.email ?: ""

    LaunchedEffect(key1 = user?.uid) {
        db.collection("usuarios").document(user!!.uid).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                nombre = document.getString("nombre") ?: ""
                direccion = document.getString("direccion") ?: ""
                telefono = document.getString("telefono") ?: ""
            } else {
                db.collection("usuarios").document(user.uid).set(
                    mapOf(
                        "correo" to correo,
                        "nombre" to nombre,
                        "direccion" to direccion,
                        "telefono" to telefono
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = correo,
            onValueChange = {},
            label = { Text("Correo") },
            enabled = false // No se puede actualizar el correo
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val userInfo = mapOf(
                "nombre" to nombre,
                "direccion" to direccion,
                "telefono" to telefono
            )
            db.collection("usuarios").document(user!!.uid).update(userInfo)
                .addOnSuccessListener {
                    // Navegar a Home o mostrar un mensaje de éxito
                    navController.navigate(Screen.Home.route)
                }
        }) {
            Text(text = "Guardar")
        }
    }
}