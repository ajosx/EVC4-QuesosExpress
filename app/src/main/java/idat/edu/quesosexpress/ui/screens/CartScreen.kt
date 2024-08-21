package idat.edu.quesosexpress.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import idat.edu.quesosexpress.ui.navigation.Screen
import idat.edu.quesosexpress.ui.utils.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    preferenceManager: PreferenceManager
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val db = Firebase.firestore
    var products by remember { mutableStateOf(listOf<Map<String, Any>>()) }
    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val productList = db.collection("productos").get().await().documents.map { document ->
            document.data?.plus("id" to document.id) ?: emptyMap<String, Any>()
        }
        products = productList.filter { product ->
            cartItems.containsKey(product["id"] as? String ?: "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products.size) { index ->
                val product = products[index]
                val productId = product["id"] as? String ?: ""
                val quantity = cartItems[productId] ?: 0
                val productName = product["nombre"] as? String ?: "Producto sin nombre"
                val productDescription = product["descripcion"] as? String ?: "Sin descripción"
                val productPrice = when (val price = product["precio"]) {
                    is Long -> price.toDouble()
                    is Double -> price
                    else -> 0.0
                }

                ProductItem(product = product, initialQuantity = quantity, productPrice = productPrice) { newQuantity ->
                    cartViewModel.updateItem(productId, newQuantity, productName, productDescription, productPrice)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                showDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Terminar Compra")
        }
    }

    if (showDialog) {
        ShippingInfoDialog(navController) {
            showDialog = false
            preferenceManager.clearProductQuantities()
            cartViewModel.clearCart()
        }
    }
}

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> get() = _cartItems

    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> get() = _cartTotal

    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser

    fun updateItem(productId: String, quantity: Int, productName: String, productDescription: String, productPrice: Double) {
        val currentCart = _cartItems.value.toMutableMap()
        if (quantity > 0) {
            currentCart[productId] = quantity
        } else {
            currentCart.remove(productId)
        }
        _cartItems.value = currentCart
        viewModelScope.launch {
            calculateTotal()
        }

        saveToFirebase(productId, quantity, productName, productDescription, productPrice)
    }

    private fun saveToFirebase(productId: String, quantity: Int, productName: String, productDescription: String, productPrice: Double) {
        val cartItem = mapOf(
            "cantidad" to quantity,
            "nombre" to productName,
            "descripcion" to productDescription,
            "precio" to productPrice
        )

        db.collection("carrito_usuario").document(user!!.uid)
            .collection("items").document(productId)
            .set(cartItem)
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _cartTotal.value = 0.0
        clearFirebaseCart()
    }

    private fun clearFirebaseCart() {
        user?.let { currentUser ->
            db.collection("carrito_usuario").document(currentUser.uid)
                .collection("items").get().addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        document.reference.delete()
                    }
                }
        }
    }

    private suspend fun calculateTotal() {
        _cartTotal.value = _cartItems.value.entries.sumOf { (productId, quantity) ->
            val productPrice = db.collection("productos").document(productId).get().await().getDouble("precio") ?: 0.0
            quantity * productPrice
        }
    }
}

@Composable
fun ShippingInfoDialog(navController: NavController, onDismiss: () -> Unit) {
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
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
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Datos de envío") },
        text = {
            Column {
                Text("Nombre: $nombre")
                Text("Dirección: $direccion")
                Text("Teléfono: $telefono")
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate(Screen.Payment.route)
            }) {
                Text("Continuar")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate(Screen.Profile.route)
            }) {
                Text("Editar datos")
            }
        }
    )
}