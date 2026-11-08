package com.maranathastore.app.presentation.reportes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassErrorState
import com.maranathastore.app.presentation.components.GlassLoading
import com.maranathastore.app.presentation.components.GlassSectionHeader
import com.maranathastore.app.presentation.components.GlassStatCard
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round

@Composable
fun ReportesScreen() {
    val vm = koinViewModel<ReportesViewModel>()
    val state by vm.uiState.collectAsStateWithLifecycle()

    when {
        state.cargando -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            GlassLoading()
        }

        state.error != null -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassErrorState(message = state.error ?: "Error")
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = vm::cargar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGlass.Esmeralda,
                    contentColor = Color(0xFF00271A)
                )
            ) {
                Text("Reintentar")
            }
        }

        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassSectionHeader(
                        titulo = "Reportes locales",
                        detalle = "Calculados desde ventas y productos",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = vm::cargar) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar reportes",
                            tint = LiquidGlass.TextoSuave
                        )
                    }
                }
            }

            item {
                Reveal {
                    GlassStatCard(
                        title = "Ingresos totales",
                        value = "S/ ${formatearMonto(state.ingresosTotales)}",
                        detalle = "Ventas válidas (excluye ANULADA)",
                        icono = Icons.Default.Payments,
                        tono = LiquidGlass.VerdeNeon,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Reveal(delayMillis = 80) {
                    GlassStatCard(
                        title = "Cantidad de operaciones",
                        value = state.cantidadOperaciones.toString(),
                        detalle = "Ventas válidas registradas",
                        icono = Icons.AutoMirrored.Filled.ReceiptLong,
                        tono = LiquidGlass.Azul,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassSectionHeader(
                    titulo = "Alertas de stock",
                    detalle = "Revisa los productos que requieren reposición",
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Reveal(delayMillis = 140, modifier = Modifier.weight(1f)) {
                        GlassStatCard(
                            title = "Stock bajo",
                            value = state.productosStockBajo.toString(),
                            detalle = "Productos con stock <= 5",
                            icono = Icons.Default.WarningAmber,
                            tono = LiquidGlass.Ambar,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Reveal(delayMillis = 200, modifier = Modifier.weight(1f)) {
                        GlassStatCard(
                            title = "Agotados",
                            value = state.productosAgotados.toString(),
                            detalle = "Productos con stock == 0",
                            icono = Icons.Default.Inventory2,
                            tono = if (state.productosAgotados > 0) {
                                LiquidGlass.Peligro
                            } else {
                                LiquidGlass.Azul
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Reveal(delayMillis = 260) {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = LiquidGlass.Azul,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(
                            text = "Los indicadores se calculan en el dispositivo con los " +
                                "datos reales del backend. No se almacenan copias locales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave
                        )
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
