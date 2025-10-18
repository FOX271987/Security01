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
        private const val TAG = &quot;AuthRepository&quot;
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
    suspend fun login(email: String, password: String): Result&lt;User&gt; {
        return withContext(Dispatchers.IO) {
            try {
// Validación básica
                if (email.isBlank() || password.isBlank()) {
                    return@withContext Result.failure(
                        Exception(&quot;El email y la contraseña son obligatorios&quot;)
                    )
                }
// Validación de formato de email
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    return@withContext Result.failure(
                        Exception(&quot;El formato del email no es válido&quot;)
                    )
                }
                Log.d(TAG, &quot;Intentando login para usuario: $email&quot;)
                // NUNCA: Log.d(TAG, &quot;Password: $password&quot;) ❌
                // Creamos la petición
                val loginRequest = LoginRequest(email, password)
                // Hacemos la llamada a la API
                val response = apiService.login(loginRequest)
                // Verificamos la respuesta
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse?.success == true &amp;&amp; loginResponse.user != null) {
                        // Login exitoso - guardamos la sesión
                        val user = loginResponse.user
                        secureStorage.saveUserSession(user)
                        Log.d(TAG, &quot;Login exitoso para: $email&quot;)
                        Result.success(user)
                    } else {

                        Log.w(TAG, &quot;Login fallido: ${loginResponse?.message}&quot;)
                        Result.failure(
                            Exception(loginResponse?.message ?: &quot;Error en el login&quot;)
                        )
                    }
                } else {
                    // Error HTTP
                    val errorMessage = when (response.code()) {
                        401 -&gt; &quot;Credenciales incorrectas&quot;
                            404 -&gt; &quot;Servicio no disponible&quot;
                        500 -&gt; &quot;Error en el servidor&quot;
                        else -&gt; &quot;Error de conexión: ${response.code()}&quot;
                    }
                    Log.e(TAG, &quot;Error HTTP: ${response.code()}&quot;)
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                // Capturamos cualquier error inesperado
                Log.e(TAG, &quot;Excepción en login&quot;, e)
                Result.failure(
                    Exception(&quot;Error de conexión: ${e.localizedMessage}&quot;)
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
    suspend fun validateToken(): Result&lt;Boolean&gt; {
        return withContext(Dispatchers.IO) {
            try {

                val token = secureStorage.getToken()
                if (token == null) {
                    return@withContext Result.success(false)
                }
                val response = apiService.validateToken(&quot;Bearer $token&quot;)
                if (response.isSuccessful &amp;&amp; response.body()?.success == true) {
// Token válido, actualizamos timestamp
                    secureStorage.updateSessionTimestamp()
                    Result.success(true)
                } else {
// Token inválido, limpiamos sesión
                    logout()
                    Result.success(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, &quot;Error validando token&quot;, e)
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
    suspend fun logout(): Result&lt;Boolean&gt; {
        return withContext(Dispatchers.IO) {
            try {
                val token = secureStorage.getToken()
// Intentamos cerrar sesión en el servidor
                if (token != null) {
                    try {
                        apiService.logout(&quot;Bearer $token&quot;)
                        Log.d(TAG, &quot;Sesión cerrada en el servidor&quot;)
                    } catch (e: Exception) {
// Si falla, continuamos cerrando sesión local
                        Log.w(TAG, &quot;No se pudo cerrar sesión en servidor&quot;, e)
                    }
                }

// Limpiamos datos locales SIEMPRE
                secureStorage.clearSession()
                Log.d(TAG, &quot;Sesión local limpiada&quot;)
                Result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, &quot;Error en logout&quot;, e)
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