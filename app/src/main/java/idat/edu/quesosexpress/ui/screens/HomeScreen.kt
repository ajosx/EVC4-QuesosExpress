package idat.edu.quesosexpress.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import idat.edu.quesosexpress.ui.navigation.Screen
import kotlinx.coroutines.tasks.await
import idat.edu.quesosexpress.R
import idat.edu.quesosexpress.ui.utils.PreferenceManager

@Composable
fun HomeScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
) {
    val db = Firebase.firestore
    var products by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        val productList = db.collection("productos").get().await().documents.map { document ->
            document.data?.plus("id" to document.id) ?: emptyMap<String, Any>()
        }
        products = productList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(13.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products.size) { index ->
                val product = products[index]
                val productId = product["id"] as String
                val productName = product["nombre"] as? String ?: "Producto sin nombre"
                val productDescription = product["descripcion"] as? String ?: "Sin descripción"
                val initialQuantity = 0
                val productPrice = when (val price = product["precio"]) {
                    is Long -> price.toDouble()
                    is Double -> price
                    else -> 0.0
                }

                ProductItem(product = product, initialQuantity, productPrice) { quantity ->
                    cartViewModel.updateItem(productId, quantity, productName, productDescription, productPrice)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate(Screen.Cart.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(text = "Continuar pedido")
        }
    }
}

@Composable
fun ProductItem(
    product: Map<String, Any>,
    initialQuantity: Int,
    productPrice: Double,
    onQuantityChange: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf(initialQuantity) }

    val productId = product["id"] as String
    val productName = product["nombre"] as? String ?: "Producto sin nombre"
    val imageUrl = product["imagenurl"] as? String

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = imageUrl ?: ""),
            contentDescription = productName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .weight(0.25f)
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Column(
            modifier = Modifier.weight(0.45f)
        ) {
            Text(
                text = productName,
                fontSize = 15.sp,
                color = Color.Black
            )
            Text(
                text = "S/. $productPrice",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.3f)
        ) {
            IconButton(
                onClick = { if (quantity > 0) quantity--; onQuantityChange(quantity) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_remove),
                    contentDescription = "Remove",
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "$quantity",
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = { quantity++; onQuantityChange(quantity) }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
