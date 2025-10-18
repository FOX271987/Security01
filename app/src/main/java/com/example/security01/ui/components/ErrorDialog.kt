package com.example.security01.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
/**
 * Diálogo para mostrar errores
 *
 * ANALOGÍA: Es como una ventana emergente que aparece
 * cuando algo sale mal, mostrando qué pasó.
 */
@Composable
fun ErrorDialog(
    title: String = &quot;Error&quot;,
message: String,
icon: ImageVector = Icons.Filled.Error,
onDismiss: () -&gt; Unit,
onConfirm: () -&gt; Unit = onDismiss
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(

                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(&quot;Aceptar&quot;)
            }
        },
        dismissButton = null
    )
}