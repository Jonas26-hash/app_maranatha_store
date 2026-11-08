package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    icono: ImageVector? = null,
    tono: Color = LiquidGlass.VerdeNeon
) {
    GlassCard(modifier = modifier, glowColor = tono) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icono != null) {
                    GlassIconTile(icon = icono, tint = tono, size = 34)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = LiquidGlass.TextoSuave
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = LiquidGlass.Texto
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave
                )
            }
        }
    }
}
