package com.maranathastore.app.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass
import org.koin.compose.viewmodel.koinViewModel

/**
 * Chat de Maranatha AI con burbujas de cristal, indicador de escritura
 * animado y una barra de entrada flotante con envío neón.
 */
@Composable
fun MaranathaAIScreen() {
    val vm = koinViewModel<MaranathaAIViewModel>()
    val state by vm.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.mensajes.size, state.enviando) {
        val count = state.mensajes.size + if (state.enviando) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.mensajes.isEmpty() && !state.enviando) {
                EstadoVacioChat(
                    modifier = Modifier.align(Alignment.Center),
                    onSugerencia = vm::onEntradaChange
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.mensajes) { mensaje ->
                        BurbujaMensaje(mensaje)
                    }
                    if (state.enviando) {
                        item { IndicadorEscribiendo() }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 },
            exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 2 }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LiquidGlass.Peligro.copy(alpha = 0.14f))
                    .border(1.dp, LiquidGlass.Peligro.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .clickable { vm.limpiarError() }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.Peligro
                )
            }
        }

        BarraEntradaChat(
            valor = state.entrada,
            enviando = state.enviando,
            onValorChange = vm::onEntradaChange,
            onEnviar = vm::enviar
        )
    }
}

@Composable
private fun EstadoVacioChat(
    modifier: Modifier = Modifier,
    onSugerencia: (String) -> Unit
) {
    val sugerencias = listOf(
        "¿Cómo van las ventas de hoy?",
        "¿Qué productos tienen stock bajo?",
        "Resume el movimiento del mes"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Reveal(delayMillis = 60) {
            GlassIconTile(
                icon = Icons.Default.AutoAwesome,
                size = 64,
                tint = LiquidGlass.VerdeNeon,
                container = LiquidGlass.VerdeNeon.copy(alpha = 0.14f)
            )
        }

        Spacer(Modifier.height(18.dp))

        Reveal(delayMillis = 140) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Maranatha AI",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LiquidGlass.Texto
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Tu asistente para ventas, inventario y clientes. Pregunta lo que necesites.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LiquidGlass.TextoSuave,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sugerencias.forEachIndexed { indice, sugerencia ->
                Reveal(delayMillis = 220 + indice * 90) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSugerencia(sugerencia) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = sugerencia,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlass.Texto
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BurbujaMensaje(mensaje: MensajeChat) {
    val esUsuario = mensaje.autor == AutorMensaje.USUARIO
    val forma = if (esUsuario) {
        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 22.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 6.dp, bottomEnd = 22.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 330.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = forma,
                    clip = false,
                    ambientColor = (if (esUsuario) LiquidGlass.VerdeNeon else LiquidGlass.Azul).copy(alpha = 0.22f),
                    spotColor = (if (esUsuario) LiquidGlass.Esmeralda else LiquidGlass.Azul).copy(alpha = 0.18f)
                )
                .clip(forma)
                .background(
                    if (esUsuario) {
                        Brush.linearGradient(
                            listOf(
                                LiquidGlass.VerdeNeon.copy(alpha = 0.24f),
                                LiquidGlass.Esmeralda.copy(alpha = 0.14f)
                            )
                        )
                    } else {
                        LiquidGlass.GradienteCristal
                    }
                )
                .border(
                    width = 1.dp,
                    brush = if (esUsuario) {
                        Brush.linearGradient(
                            listOf(
                                LiquidGlass.VerdeNeon.copy(alpha = 0.55f),
                                LiquidGlass.Esmeralda.copy(alpha = 0.25f)
                            )
                        )
                    } else {
                        LiquidGlass.GradienteBorde
                    },
                    shape = forma
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (esUsuario) LiquidGlass.VerdeNeon else LiquidGlass.Azul)
                )
                Text(
                    text = if (esUsuario) "Tú" else "Maranatha AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (esUsuario) LiquidGlass.VerdeNeon else LiquidGlass.Azul
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = mensaje.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidGlass.Texto
            )
        }
    }
}

@Composable
private fun IndicadorEscribiendo() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 6.dp, bottomEnd = 22.dp))
                .background(LiquidGlass.GradienteCristal)
                .border(
                    1.dp,
                    LiquidGlass.Borde.copy(alpha = 0.40f),
                    RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 6.dp, bottomEnd = 22.dp)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(3) { indice ->
                    PuntoEscribiendo(indice)
                }
            }
            Text(
                text = "Escribiendo...",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.TextoSuave
            )
        }
    }
}

@Composable
private fun PuntoEscribiendo(indice: Int) {
    val transicion = rememberInfiniteTransition(label = "punto$indice")
    val alfa by transicion.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, delayMillis = indice * 140),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alfaPunto$indice"
    )

    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(LiquidGlass.VerdeNeon.copy(alpha = alfa))
    )
}

@Composable
private fun BarraEntradaChat(
    valor: String,
    enviando: Boolean,
    onValorChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    val habilitado = valor.trim().isNotBlank() && !enviando
    val forma = RoundedCornerShape(26.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 20.dp,
                shape = forma,
                clip = false,
                ambientColor = LiquidGlass.VerdeNeon.copy(alpha = 0.20f),
                spotColor = LiquidGlass.Azul.copy(alpha = 0.16f)
            )
            .clip(forma)
            .background(LiquidGlass.GradienteCristal)
            .border(
                width = 1.dp,
                brush = if (habilitado) {
                    LiquidGlass.GradienteBorde
                } else {
                    Brush.linearGradient(
                        listOf(
                            LiquidGlass.Borde.copy(alpha = 0.30f),
                            LiquidGlass.Borde.copy(alpha = 0.30f)
                        )
                    )
                },
                shape = forma
            )
            .padding(start = 18.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        BasicTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            enabled = !enviando,
            maxLines = 4,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = LiquidGlass.Texto),
            cursorBrush = SolidColor(LiquidGlass.VerdeNeon),
            decorationBox = { contenidoInterno ->
                Box {
                    if (valor.isEmpty()) {
                        Text(
                            text = "Escribe un mensaje...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlass.TextoSuave.copy(alpha = 0.7f)
                        )
                    }
                    contenidoInterno()
                }
            }
        )

        Box(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (habilitado) {
                        LiquidGlass.GradienteNeon
                    } else {
                        SolidColor(LiquidGlass.CristalSuave)
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (habilitado) {
                        Color.Transparent
                    } else {
                        LiquidGlass.Borde.copy(alpha = 0.25f)
                    },
                    shape = CircleShape
                )
                .clickable(enabled = habilitado, onClick = onEnviar),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Enviar",
                tint = if (habilitado) Color(0xFF00271A) else LiquidGlass.TextoSuave
            )
        }
    }
}
