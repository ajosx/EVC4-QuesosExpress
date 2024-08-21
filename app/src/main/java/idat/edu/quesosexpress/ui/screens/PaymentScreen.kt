package idat.edu.quesosexpress.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import idat.edu.quesosexpress.ui.navigation.Screen
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.platform.LocalContext

@Composable
fun PaymentScreen(navController: NavController, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    var cartTotal by remember { mutableStateOf(0.0) }
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    var products by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    var selectedPaymentMethod by remember { mutableStateOf("Yape") }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        user?.let { currentUser ->
            val items = db.collection("carrito_usuario")
                .document(currentUser.uid)
                .collection("items")
                .get().await().documents.mapNotNull { it.data }
            products = items
            cartTotal = items.sumOf { it["precio"] as Double * (it["cantidad"] as Long).toInt() }

            val userDoc = db.collection("usuarios").document(currentUser.uid).get().await()
            direccion = userDoc.getString("direccion") ?: ""
            telefono = userDoc.getString("telefono") ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Resumen del Pedido", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar nombre, cantidad y precio de los productos seleccionados
        products.forEach { product ->
            val productName = product["nombre"] as? String ?: "Producto desconocido"
            val quantity = (product["cantidad"] as? Long)?.toInt() ?: 0
            val price = product["precio"] as? Double ?: 0.0
            Text(text = "$productName x $quantity = S/. ${quantity * price}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar el costo total
        Text(text = "Total: S/. $cartTotal", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Método de Pago")
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selectedPaymentMethod == "Yape",
                onClick = { selectedPaymentMethod = "Yape" }
            )
            Text(text = "Yape")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(
                selected = selectedPaymentMethod == "Plin",
                onClick = { selectedPaymentMethod = "Plin" }
            )
            Text(text = "Plin")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(
                selected = selectedPaymentMethod == "Efectivo",
                onClick = { selectedPaymentMethod = "Efectivo" }
            )
            Text(text = "Efectivo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Cargando Pedido...", Toast.LENGTH_SHORT).show()

                // Crear la lista de productos para almacenar en la colección 'ordenes'
                val productosList = products.map { product ->
                    mapOf(
                        "nombre" to (product["nombre"] as? String ?: "Producto desconocido"),
                        "cantidad" to (product["cantidad"] as? Long ?: 0)
                    )
                }

                // Guardar la orden en Firestore
                db.collection("ordenes").add(
                    mapOf(
                        "idcliente" to user?.uid,
                        "nombrecliente" to (user?.displayName ?: ""),
                        "direccioncliente" to direccion,
                        "telefonocliente" to telefono,
                        "correocliente" to (user?.email ?: ""),
                        "metodopago" to selectedPaymentMethod,
                        "totalcompra" to cartTotal,
                        "productos" to productosList
                    )
                ).addOnSuccessListener {
                    // Limpiar el carrito después del pago
                    cartViewModel.clearCart()
                    navController.navigate(Screen.OrderHistory.route)
                }.addOnFailureListener { e ->
                    Toast.makeText(context, "Error al guardar la orden: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Pagar Ya")
        }
    }
}