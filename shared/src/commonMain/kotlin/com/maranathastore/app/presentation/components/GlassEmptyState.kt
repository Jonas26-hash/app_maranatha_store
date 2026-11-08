package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    titulo: String = "Sin resultados"
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = LiquidGlass.Texto
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.TextoSuave,
                textAlign = TextAlign.Center
            )
        }
    }
}
