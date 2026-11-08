package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    loadingText: String = "Procesando…"
) {
    val forma = RoundedCornerShape(18.dp)

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = 16.dp,
                shape = forma,
                clip = false,
                ambientColor = LiquidGlass.VerdeNeon.copy(alpha = 0.45f),
                spotColor = LiquidGlass.Azul.copy(alpha = 0.30f)
            ),
        shape = forma,
        colors = ButtonDefaults.buttonColors(
            containerColor = LiquidGlass.Esmeralda,
            contentColor = Color(0xFF00271A),
            disabledContainerColor = LiquidGlass.CristalSuave,
            disabledContentColor = LiquidGlass.TextoSuave
        )
    ) {
        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LiquidGlass.VerdeNeon
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
