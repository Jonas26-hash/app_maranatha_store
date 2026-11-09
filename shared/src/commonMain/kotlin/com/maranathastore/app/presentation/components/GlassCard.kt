package com.maranathastore.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

/**
 * Tarjeta de vidrio translúcido con borde luminoso de 1.dp y esquinas de 20.dp.
 * Es el bloque base de todas las superficies de la app.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = LiquidGlass.GradienteBorde,
    glowColor: Color = LiquidGlass.VerdeNeon,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    fondoSolido: Boolean = false,
    colorDeFondo: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseConSombra = modifier
        .shadow(
            elevation = 18.dp,
            shape = shape,
            clip = false,
            ambientColor = glowColor.copy(alpha = 0.20f),
            spotColor = glowColor.copy(alpha = 0.16f)
        )
        .clip(shape)

    val baseConFondo = if (fondoSolido) {
        baseConSombra.background(colorDeFondo ?: LiquidGlass.SuperficieDialogo.copy(alpha = 0.92f))
    } else {
        baseConSombra.background(brush = LiquidGlass.GradienteCristal)
    }
        .border(1.dp, borderBrush, shape)

    Column(
        modifier = (if (onClick != null) baseConFondo.clickable(onClick = onClick) else baseConFondo)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = LiquidGlass.VerdeNeon,
    container: Color = LiquidGlass.VerdeNeon.copy(alpha = 0.12f),
    size: Int = 44
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(container)
            .border(1.dp, tint.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size((size * 0.48f).dp)
        )
    }
}

@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LiquidGlass.VerdeNeon,
    container: Color = LiquidGlass.VerdeNeon.copy(alpha = 0.12f)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun GlassSectionHeader(
    titulo: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
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
        accion?.invoke()
    }
}
