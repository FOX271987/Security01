package com.example.security01.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.security01.R
import com.example.security01.models.AuthState
import com.example.security01.ui.components.CustomTextField
import com.example.security01.ui.components.ErrorDialog
import com.example.security01.ui.components.LoadingButton
import com.example.security01.viewmodel.AuthViewModel
/**
 * Pantalla de Login con Jetpack Compose
 *
 * ESTRUCTURA DE UNA PANTALLA COMPOSE:
 * 1. Estados locales (remember)
 * 2. Observación de ViewModels
 * 3. UI (interfaz visual)
 * 4. Efectos secundarios (LaunchedEffect, SideEffect)
 *
 * ANALOGÍA PARA ESTUDIANTES:
 * Imaginen que están montando una obra de teatro:
 * - Estados = Los diálogos que van cambiando
 * - ViewModel = El director que coordina todo
 * - UI = El escenario y los actores
 * - Efectos = Las luces y sonido que reaccionan a la acción
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -&gt; Unit
) {
// ============================================
// PASO 1: ESTADOS LOCALES
// ============================================
// Estados para los campos de texto
    var email by remember { mutableStateOf(&quot;&quot;) }
    var password by remember { mutableStateOf(&quot;&quot;) }
// Estados para validaciones
    var emailError by remember { mutableStateOf&lt;String?&gt;(null) }
    var passwordError by remember { mutableStateOf&lt;String?&gt;(null) }
// Estado para mostrar diálogo de error
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf(&quot;&quot;) }
// ============================================
// PASO 2: OBSERVACIÓN DEL VIEWMODEL
// ============================================
    val authState by viewModel.authState.collectAsState()

// ============================================
// PASO 3: EFECTOS SECUNDARIOS
// ============================================
    /**
     * LaunchedEffect observa cambios en authState
     *
     * EXPLICACIÓN: Es como un &quot;vigilante&quot; que está atento
     * a cambios específicos y reacciona cuando ocurren.
     */
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -&gt; {
// Login exitoso - navegamos a la siguiente pantalla
            onLoginSuccess()
        }
                is AuthState.Error -&gt; {
// Mostramos el error
            errorMessage = (authState as AuthState.Error).message
            showErrorDialog = true
            viewModel.resetAuthState()
        }
            else -&gt; {
// Idle o Loading - no hacemos nada
        }
        }
    }
// ============================================
// PASO 4: FUNCIÓN DE VALIDACIÓN
// ============================================
    /**
     * Valida los campos antes de enviar
     *
     * IMPORTANTE: Siempre validar en el cliente Y en el servidor
     * Cliente: Para dar feedback inmediato al usuario
     * Servidor: Por seguridad (nunca confiar solo en el cliente)
     */
    fun validateFields(): Boolean {
        var isValid = true
// Validar email
        when {
            email.isBlank() -&gt; {
                emailError = &quot;El email es obligatorio&quot;
                isValid = false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -&gt; {
                emailError = &quot;Formato de email inválido&quot;

                isValid = false
            }
            else -&gt; emailError = null
        }
// Validar password
        when {
            password.isBlank() -&gt; {
                passwordError = &quot;La contraseña es obligatoria&quot;
                isValid = false
            }
            password.length &lt; 6 -&gt; {
            passwordError = &quot;Mínimo 6 caracteres&quot;
            isValid = false
        }
            else -&gt; passwordError = null
        }
        return isValid
    }
    // ============================================
// PASO 5: FUNCIÓN DE LOGIN
// ============================================
    fun performLogin() {
        if (validateFields()) {
            viewModel.login(email, password)
        }
    }
// ============================================
// PASO 6: UI - INTERFAZ VISUAL
// ============================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
// Hacemos la columna scrolleable por si el teclado cubre contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
// ============================================
// LOGO Y TÍTULOS

// ============================================
            Image(
                painter = painterResource(id = R.drawable.ic_security),
                contentDescription = &quot;Logo de la app&quot;,
            modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = &quot;Bienvenido&quot;,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = &quot;Inicia sesión para continuar&quot;,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))
// ============================================
// CAMPOS DE TEXTO
// ============================================
            CustomTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null // Limpiamos el error al escribir
                },
                label = &quot;Correo electrónico&quot;,
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            isError = emailError != null,
            errorMessage = emailError,
            enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(
                value = password,

                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = &quot;Contraseña&quot;,
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            onImeAction = { performLogin() },
            isError = passwordError != null,
            errorMessage = passwordError,
            enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(32.dp))
// ============================================
// BOTÓN DE LOGIN
// ============================================
            LoadingButton(
                text = &quot;Iniciar Sesión&quot;,
            onClick = { performLogin() },
            isLoading = authState is AuthState.Loading,
            enabled = authState !is AuthState.Loading
            )
            Spacer(modifier = Modifier.height(16.dp))
// ============================================
// TEXTO DE AYUDA (opcional)
// ============================================
            TextButton(
                onClick = { /* TODO: Navegar a recuperar contraseña */ },
                enabled = authState !is AuthState.Loading
            ) {
                Text(
                    text = &quot;¿Olvidaste tu contraseña?&quot;,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
// ============================================

// VERSIÓN DE LA APP (Info adicional)
// ============================================
            Text(
                text = &quot;Versión 1.0.0&quot;,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
// ============================================
// DIÁLOGO DE ERROR
// ============================================
        if (showErrorDialog) {
            ErrorDialog(
                title = &quot;Error de autenticación&quot;,
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
            )
        }
    }
}