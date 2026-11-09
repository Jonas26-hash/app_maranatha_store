package com.maranathastore.app.presentation.ventas.historial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassEmptyState
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassPill
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round

@Composable
fun HistorialVentasScreen() {
    val vm = koinViewModel<HistorialVentasViewModel>()
    val ventas by vm.ventas.collectAsStateWithLifecycle()

    var ventaAAnular by remember { mutableStateOf<VentaDto?>(null) }
    var ventaDetalle by remember { mutableStateOf<VentaDto?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${ventas.size} operaciones",
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = "Ventas emitidas y anulaciones",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave
                )
            }
            IconButton(onClick = { vm.cargar() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Actualizar historial",
                    tint = LiquidGlass.TextoSuave
                )
            }
        }

        if (ventas.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlassIconTile(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = LiquidGlass.Azul,
                    container = LiquidGlass.Azul.copy(alpha = 0.12f),
                    size = 56
                )
                Spacer(Modifier.height(14.dp))
                GlassEmptyState(
                    titulo = "Sin ventas registradas",
                    message = "Las ventas que emitas aparecerán aquí con su estado."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = ventas,
                    key = { indice, venta -> venta.id ?: indice }
                ) { indice, venta ->
                    Reveal(delayMillis = (indice.coerceAtMost(9)) * 40) {
                        VentaCard(
                            venta = venta,
                            onAnular = { ventaAAnular = venta },
                            onVentaClick = { ventaDetalle = venta }
                        )
                    }
                }
            }
        }
    }

    ventaAAnular?.let { venta ->
        AlertDialog(
            onDismissRequest = { ventaAAnular = null },
            containerColor = LiquidGlass.SuperficieDialogo,
            titleContentColor = LiquidGlass.Texto,
            textContentColor = LiquidGlass.TextoSuave,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Anular venta") },
            text = {
                Text(
                    text = "¿Anular la venta #${venta.id} por " +
                        "S/ ${formatearMonto(venta.total ?: 0.0)}? " +
                        "Esta acción queda registrada en el backend."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        venta.id?.let { vm.anular(it) }
                        ventaAAnular = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGlass.Peligro,
                        contentColor = Color(0xFF2A060C)
                    )
                ) {
                    Text("Anular")
                }
            },
            dismissButton = {
                TextButton(onClick = { ventaAAnular = null }) {
                    Text("Cancelar", color = LiquidGlass.TextoSuave)
                }
            }
        )
    }

    ventaDetalle?.let { venta ->
        Dialog(
            onDismissRequest = { ventaDetalle = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF000000).copy(alpha = 0.45f))
            ) {
                VentaDetalleSheet(
                    venta = venta,
                    onCerrar = { ventaDetalle = null }
                )
            }
        }
    }
}

@Composable
private fun VentaDetalleSheet(
    venta: VentaDto,
    onCerrar: () -> Unit
) {
    val anulada = venta.estado.equals("ANULADA", ignoreCase = true)
    val tono = if (anulada) LiquidGlass.Peligro else LiquidGlass.VerdeNeon

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .heightIn(max = 520.dp),
        contentPadding = PaddingValues(20.dp),
        glowColor = tono,
        fondoSolido = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassIconTile(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = tono,
                    container = tono.copy(alpha = 0.12f),
                    size = 56
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${venta.id}",
                        style = MaterialTheme.typography.labelLarge,
                        color = LiquidGlass.TextoSuave
                    )
                    Text(
                        text = venta.tipoComprobante ?: "Comprobante",
                        style = MaterialTheme.typography.titleLarge,
                        color = LiquidGlass.Texto
                    )
                }
            }

            Text(
                text = venta.fecha?.replace("T", " ")?.take(16) ?: "Sin fecha",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.TextoSuave
            )

            HorizontalDivider(color = LiquidGlass.Borde.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassPill(
                    text = "Estado",
                    color = tono,
                    container = tono.copy(alpha = 0.12f)
                )
                GlassPill(
                    text = venta.estado ?: "REGISTRADA",
                    color = tono,
                    container = tono.copy(alpha = 0.12f)
                )
                GlassPill(
                    text = "S/ ${formatearMonto(venta.total ?: 0.0)}",
                    color = LiquidGlass.VerdeNeon,
                    container = LiquidGlass.VerdeNeon.copy(alpha = 0.12f)
                )
            }

            Text(
                text = venta.clienteId?.let { "Cliente #$it" } ?: "Sin cliente asignado",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidGlass.TextoSuave
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                TextButton(onClick = onCerrar) {
                    Text("Cerrar", color = LiquidGlass.TextoSuave)
                }
            }
        }
    }
}

@Composable
private fun VentaCard(
    venta: VentaDto,
    onAnular: () -> Unit,
    onVentaClick: () -> Unit
) {
    val anulada = venta.estado.equals("ANULADA", ignoreCase = true)
    val tono = if (anulada) LiquidGlass.Peligro else LiquidGlass.VerdeNeon

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = tono,
        onClick = onVentaClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassPill(
                    text = venta.tipoComprobante ?: "COMPROBANTE",
                    color = LiquidGlass.Azul,
                    container = LiquidGlass.Azul.copy(alpha = 0.12f)
                )
                Text(
                    text = "#${venta.id}",
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.TextoSuave
                )
                Spacer(Modifier.weight(1f))
                GlassPill(
                    text = venta.estado ?: "REGISTRADA",
                    color = tono,
                    container = tono.copy(alpha = 0.12f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "S/ ${formatearMonto(venta.total ?: 0.0)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (anulada) LiquidGlass.TextoSuave else LiquidGlass.VerdeNeon
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = venta.fecha?.replace("T", " ")?.take(16) ?: "Sin fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = venta.clienteId?.let { "Cliente #$it" } ?: "Sin cliente asignado",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    modifier = Modifier.weight(1f)
                )
                if (!anulada) {
                    TextButton(onClick = onAnular) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = LiquidGlass.Peligro,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("Anular", color = LiquidGlass.Peligro)
                    }
                }
            }
        }
    }
}

private fun formatearMonto(valor: Double): String {
    val redondeado = round(valor * 100) / 100.0
    val entero = redondeado.toLong()
    val decimales = ((redondeado - entero) * 100).toInt().let { if (it < 0) -it else it }
    return "$entero.${decimales.toString().padStart(2, '0')}"
}
