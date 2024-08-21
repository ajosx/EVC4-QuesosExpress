package idat.edu.quesosexpress.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

@Composable
fun OrderHistoryScreen(navController: NavController, cartViewModel: CartViewModel) {
    val db = Firebase.firestore
    var orders by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        val orderList = db.collection("ordenes")
            .whereEqualTo("idcliente", Firebase.auth.currentUser?.uid)
            .get().await().documents.mapNotNull { it.data }
        orders = orderList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Historial de Órdenes", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders.size) { index ->
                val order = orders[index]
                OrderItem(order)
            }
        }
    }
}

@Composable
fun OrderItem(order: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Nombre: ${order["nombrecliente"]}")
            Text(text = "Dirección: ${order["direccioncliente"]}")
            Text(text = "Teléfono: ${order["telefonocliente"]}")
            Text(text = "Correo: ${order["correocliente"]}")
            Text(text = "Método de Pago: ${order["metodopago"]}")
            val productos = order["productos"] as? List<Map<String, Any>>
            productos?.forEach { product ->
                val productName = product["nombre"] as? String ?: "Producto desconocido"
                val quantity = (product["cantidad"] as? Long) ?: 0
                Text(text = "Producto: $productName - Cantidad: $quantity")
            }
            Text(text = "Total: S/. ${order["totalcompra"]}")

        }
    }
}