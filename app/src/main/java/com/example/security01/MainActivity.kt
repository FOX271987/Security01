package com.example.security01

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.security01.navigation.NavigationGraph
import com.example.security01.navigation.Screen
import com.example.security01.ui.theme.Security01Theme
import com.example.security01.viewmodel.AuthViewModel
/**
 * Activity Principal de la aplicación
 *
 * Explicación:
 * En Compose, la Activity es MUCHO más simple que en Views tradicionales.
 * Solo hace dos cosas principales:
 * 1. Configura el tema
 * 2. Inicia el grafo de navegación
 *
 * Todo lo demás (UI, lógica) está en Composables separados.
 * Esto hace el código más limpio y modular.
 */
class MainActivity : ComponentActivity() {
    // ViewModel compartido en toda la app
    private val authViewModel: AuthViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
// Instalamos el splash screen del sistema
        installSplashScreen()
        super.onCreate(savedInstanceState)
        /**
         * setContent {} es donde &quot;arranca&quot; Compose
         *
         * IMPORTANTE: Todo lo que esté dentro de setContent
         * será UI declarativa con Compose.
         */
        setContent {
            Security01Theme {
// Surface es el contenedor base
                Surface(
                    modifier = Modifier.fillMaxSize(),

                    color = MaterialTheme.colorScheme.background
                ) {
                    SecurityApp()
                }
            }
        }
    }
}
/**
 * Composable principal de la aplicación
 *
 * ESTRUCTURA:
 * MainActivity -&gt; SecurityApp -&gt; NavigationGraph -&gt; Screens
 *
 * Es como una pirámide: la base (MainActivity) sostiene todo lo demás.
 */
@Composable
fun SecurityApp() {
    val navController = rememberNavController()
    NavigationGraph(
        navController = navController,
        startDestination = Screen.Splash.route
    )
}