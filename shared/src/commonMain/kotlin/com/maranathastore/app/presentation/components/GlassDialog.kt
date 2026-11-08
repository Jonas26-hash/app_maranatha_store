package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = LiquidGlass.SuperficieDialogo,
        titleContentColor = LiquidGlass.Texto,
        textContentColor = LiquidGlass.TextoSuave,
        shape = MaterialTheme.shapes.large,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text(message)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar", color = LiquidGlass.VerdeNeon)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = LiquidGlass.TextoSuave)
            }
        }
    )
}
