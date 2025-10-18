package com.example.security01.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.security01.models.LoginRequest
import com.example.security01.models.User
import com.example.security01.network.RetrofitClient
import com.example.security01.security.SecureStorage
/**
 * Repositorio de Autenticación
 *
 * PATRÓN REPOSITORY EXPLICADO:
 * Imaginen una biblioteca. Ustedes (UI) no van directamente a buscar
 * entre millones de libros. Le piden al bibliotecario (Repository)
 * que lo haga por ustedes. El bibliotecario sabe dónde buscar: en los
 * estantes (API) o en el archivero local (SecureStorage).
 */
class AuthRepository(context: Context) {
    private val secureStorage = SecureStorage(context)
    private val apiService = RetrofitClient.apiService
    companion object {
        private const val TAG = "AuthRepository"
// NUNCA hacer Log.d() con tokens o passwords
    }

    /**
     * Realiza el login del usuario
     *
     * PROCESO PASO A PASO:
     * 1. Valida que los campos no estén vacíos
     * 2. Envía credenciales al servidor
     * 3. Si es exitoso, guarda la sesión localmente
     * 4. Retorna el resultado
     */
    suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
// Validación básica
                if (email.isBlank() || password.isBlank()) {
                    return@withContext Result.failure(
                        Exception("El email y la contraseña son obligatorios")
                    )
                }
// Validación de formato de email
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    return@withContext Result.failure(
                        Exception("El formato del email no es válido")
                    )
                }
                Log.d(TAG, "Intentando login para usuario: $email")
                // NUNCA: Log.d(TAG, &quot;Password: $password&quot;) ❌
                // Creamos la petición
                val loginRequest = LoginRequest(email, password)
                // Hacemos la llamada a la API
                val response = apiService.login(loginRequest)
                // Verificamos la respuesta
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true && loginResponse.user != null) {
                        // Login exitoso - guardamos la sesión
                        val user = loginResponse.user
                        secureStorage.saveUserSession(user)
                        Log.d(TAG, "Login exitoso para: $email")
                        Result.success(user)
                    } else {

                        Log.w(TAG, "Login fallido: ${loginResponse?.message}")
                        Result.failure(
                            Exception(loginResponse?.message ?: "Error en el login")
                        )
                    }
                } else {
                    // Error HTTP
                    val errorMessage = when (response.code()) {
                        401 -> "Credenciales incorrectas"
                            404 -> "Servicio no disponible"
                        500 -> "Error en el servidor"
                        else -> "Error de conexión: ${response.code()}"
                    }
                    Log.e(TAG, "Error HTTP: ${response.code()}")
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Capturamos cualquier error inesperado
                Log.e(TAG, "Excepción en login", e)
                Result.failure(
                    Exception("Error de conexión: ${e.localizedMessage}")
                )
            }
        }
    }
    /**
     * Verifica si hay una sesión activa guardada localmente
     */
    fun isLoggedIn(): Boolean {
        return secureStorage.isLoggedIn()
    }
    /**
     * Obtiene los datos del usuario actual
     */
    fun getCurrentUser(): User? {
        return secureStorage.getUserData()
    }
    /**
     * Valida el token con el servidor
     * Útil para verificar si la sesión sigue siendo válida
     */
    suspend fun validateToken(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {

                val token = secureStorage.getToken()
                if (token == null) {
                    return@withContext Result.success(false)
                }
                val response = apiService.validateToken("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
// Token válido, actualizamos timestamp
                    secureStorage.updateSessionTimestamp()
                    Result.success(true)
                } else {
// Token inválido, limpiamos sesión
                    logout()
                    Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error validando token", e)
                Result.failure(e)
            }
        }
    }
    /**
     * Cierra la sesión del usuario
     *
     * IMPORTANTE: Siempre debe limpiar:
     * 1. Datos locales
     * 2. Sesión en el servidor (si aplica)
     */
    suspend fun logout(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getToken()
// Intentamos cerrar sesión en el servidor
                if (token != null) {
                    try {
                        apiService.logout("Bearer $token")
                        Log.d(TAG, "Sesión cerrada en el servidor")
                    } catch (e: Exception) {
// Si falla, continuamos cerrando sesión local
                        Log.w(TAG, "No se pudo cerrar sesión en servidor", e)
                    }
                }

// Limpiamos datos locales SIEMPRE
                secureStorage.clearSession()
                Log.d(TAG, "Sesión local limpiada")
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error en logout", e)
// Aún así limpiamos local
                secureStorage.clearSession()
                Result.failure(e)
            }
        }
    }
    /**
     * Actualiza el timestamp de actividad del usuario
     * Llamar en eventos importantes de la app
     */
    fun updateActivity() {
        secureStorage.updateSessionTimestamp()
    }
}