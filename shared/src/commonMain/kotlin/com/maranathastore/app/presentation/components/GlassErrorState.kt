package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = LiquidGlass.Peligro,
        borderBrush = androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(
                LiquidGlass.Peligro.copy(alpha = 0.55f),
                LiquidGlass.Borde.copy(alpha = 0.25f)
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = LiquidGlass.Peligro
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.Peligro,
                textAlign = TextAlign.Center
            )
        }
    }
}
