package com.maranathastore.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maranathastore.app.navigation.DestinoApp
import com.maranathastore.app.theme.LiquidGlass

private val DestinosBarra = listOf(
    DestinoApp.INICIO,
    DestinoApp.PRODUCTOS,
    DestinoApp.NUEVA_VENTA,
    DestinoApp.HISTORIAL,
    DestinoApp.MARANATHA_AI
)

private fun etiquetaCorta(destino: DestinoApp): String = when (destino) {
    DestinoApp.INICIO -> "Inicio"
    DestinoApp.PRODUCTOS -> "Productos"
    DestinoApp.NUEVA_VENTA -> "Venta"
    DestinoApp.HISTORIAL -> "Historial"
    DestinoApp.MARANATHA_AI -> "IA"
    else -> destino.titulo
}

/**
 * Barra inferior flotante de cristal, separada del borde de la pantalla.
 * El indicador se desliza con un spring suave al cambiar de pestaña.
 */
@Composable
fun GlassNavigationBar(
    destinoActual: DestinoApp,
    onNavegar: (DestinoApp) -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 26.dp,
                    shape = forma,
                    clip = false,
                    ambientColor = LiquidGlass.VerdeNeon.copy(alpha = 0.30f),
                    spotColor = LiquidGlass.Azul.copy(alpha = 0.22f)
                )
                .clip(forma)
                .background(LiquidGlass.GradienteBarra)
                .border(1.dp, LiquidGlass.GradienteBorde, forma)
                .padding(5.dp)
        ) {
            val anchoItem = maxWidth / DestinosBarra.size
            val indiceReal = DestinosBarra.indexOf(destinoActual)
            val indice = indiceReal.coerceAtLeast(0)
            val desplazamiento by animateDpAsState(
                targetValue = anchoItem * indice,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "indicadorNav"
            )
            val alfaIndicador by animateFloatAsState(
                targetValue = if (indiceReal >= 0) 1f else 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "alfaIndicadorNav"
            )

            Box(
                modifier = Modifier
                    .offset(x = desplazamiento)
                    .width(anchoItem)
                    .height(54.dp)
                    .padding(horizontal = 4.dp)
                    .graphicsLayer { alpha = alfaIndicador }
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LiquidGlass.VerdeNeon.copy(alpha = 0.22f),
                                LiquidGlass.Azul.copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = LiquidGlass.VerdeNeon.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(18.dp)
                    )
            )

            Row(modifier = Modifier.fillMaxWidth().height(54.dp)) {
                DestinosBarra.forEach { destino ->
                    val seleccionado = destino == destinoActual
                    val color by animateColorAsState(
                        targetValue = if (seleccionado) LiquidGlass.VerdeNeon else LiquidGlass.TextoSuave,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "colorNav"
                    )
                    val escala by animateFloatAsState(
                        targetValue = if (seleccionado) 1.04f else 0.94f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "escalaNav"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { onNavegar(destino) }
                            .graphicsLayer {
                                scaleX = escala
                                scaleY = escala
                            }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = iconoDe(destino),
                            contentDescription = destino.titulo,
                            tint = color,
                            modifier = Modifier.height(21.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = etiquetaCorta(destino),
                            color = color,
                            fontSize = 9.sp,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
