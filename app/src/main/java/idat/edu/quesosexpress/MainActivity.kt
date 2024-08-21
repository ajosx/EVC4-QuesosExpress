package idat.edu.quesosexpress
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import idat.edu.quesosexpress.ui.components.MenuDrawer
import idat.edu.quesosexpress.ui.navigation.NavGraph
import idat.edu.quesosexpress.ui.navigation.Screen
import idat.edu.quesosexpress.ui.screens.CartViewModel
import idat.edu.quesosexpress.ui.theme.QuesosExpressTheme
import idat.edu.quesosexpress.ui.utils.PreferenceManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavHostController
    private lateinit var preferenceManager: PreferenceManager
    private val cartViewModel: CartViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        preferenceManager = PreferenceManager(this)
        val signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            handleSignInResult(result)
        }
        val signInIntent = getGoogleSignInRequest()

        setContent {
            QuesosExpressTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    navController = rememberNavController()
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    var showMenu by remember { mutableStateOf(false) }  // Declaración de showMenu

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            MenuDrawer(
                                navController = navController,
                                onCloseDrawer = { scope.launch { drawerState.close() } }
                            )
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = "Quesos Express",
                                            modifier = Modifier.clickable {
                                                navController.navigate(Screen.Home.route)
                                            }
                                        )
                                    },
                                    actions = {
                                        if (showMenu) {
                                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary)
                                        .fillMaxWidth()
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        ) { contentPadding ->
                            NavGraph(
                                navController = navController,
                                signInLauncher = signInLauncher,
                                signInIntent = signInIntent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding)
                                    .background(MaterialTheme.colorScheme.background),
                                onLoginScreen = { isLoginScreen ->
                                    showMenu = !isLoginScreen  // Manejo de showMenu según la pantalla
                                },
                                cartViewModel = cartViewModel,
                                preferenceManager = preferenceManager
                            )

                            // Navegar a Home si ya hay una sesión activa
                            LaunchedEffect(auth.currentUser) {
                                if (auth.currentUser != null) {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getGoogleSignInRequest(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        return googleSignInClient.signInIntent
    }

    private fun handleSignInResult(result: ActivityResult) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { authResult ->
                    if (authResult.isSuccessful) {
                        val user = auth.currentUser
                        val db = Firebase.firestore

                        // Check if the user is already registered in Firestore
                        db.collection("usuarios").document(user!!.uid).get()
                            .addOnSuccessListener { document ->
                                if (document == null || !document.exists()) {
                                    val userInfo = mapOf(
                                        "correo" to user.email,
                                        "nombre" to "",
                                        "direccion" to "",
                                        "telefono" to ""
                                    )
                                    db.collection("usuarios").document(user.uid).set(userInfo)
                                        .addOnSuccessListener {
                                            // Successfully registered, navigate to HomeScreen
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Login.route) { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener {
                                            // Handle Firestore registration error
                                        }
                                } else {
                                    // User is already registered, navigate to HomeScreen
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                // Handle Firestore get document error
                            }
                    } else {
                        // Handle sign-in error
                    }
                }
        }
    }
}