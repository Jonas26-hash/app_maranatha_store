package com.maranathastore.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassTopBar(
    titulo: String,
    subtitulo: String,
    onMenu: () -> Unit,
    onMaranathaAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onMenu,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(LiquidGlass.CristalSuave)
                    .border(1.dp, LiquidGlass.Borde.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Abrir menú",
                    tint = LiquidGlass.Texto
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(LiquidGlass.VerdeNeon.copy(alpha = 0.14f))
                    .border(1.dp, LiquidGlass.VerdeNeon.copy(alpha = 0.45f), CircleShape)
            ) {
                IconButton(onClick = onMaranathaAi, modifier = Modifier.size(42.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Abrir Maranatha AI",
                        tint = LiquidGlass.VerdeNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
