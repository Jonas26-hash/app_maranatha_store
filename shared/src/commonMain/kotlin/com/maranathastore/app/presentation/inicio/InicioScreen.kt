package com.maranathastore.app.presentation.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.domain.venta.Venta
import com.maranathastore.app.navigation.DestinoApp
import com.maranathastore.app.presentation.components.GlassButton
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassEmptyState
import com.maranathastore.app.presentation.components.GlassErrorState
import com.maranathastore.app.presentation.components.GlassIconButton
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassLoading
import com.maranathastore.app.presentation.components.GlassPill
import com.maranathastore.app.presentation.components.GlassSectionHeader
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.presentation.dashboard.DashboardUiState
import com.maranathastore.app.presentation.dashboard.DashboardViewModel
import com.maranathastore.app.presentation.dashboard.ProductoAlerta
import com.maranathastore.app.presentation.dashboard.PuntoVentaDiaria
import com.maranathastore.app.theme.LiquidGlass
import com.maranathastore.app.util.fechaLargaEspanol
import com.maranathastore.app.util.horaDelDia
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.roundToLong

@Composable
fun InicioScreen(
    sesion: Sesion,
    onNavegar: (DestinoApp) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = koinViewModel()
) {
    val estado by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargar()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val alertas = (estado as? DashboardUiState.Success)
                ?.let { it.stockBajo + it.stockAgotado }
                ?: 0

            EncabezadoDashboard(
                sesion = sesion,
                alertas = alertas,
                onNotificaciones = { onNavegar(DestinoApp.PRODUCTOS) }
            )

            when (val actual = estado) {
                DashboardUiState.Loading -> CargandoDashboard()

                is DashboardUiState.Error -> ErrorDashboard(
                    mensaje = actual.mensaje,
                    onReintentar = { viewModel.cargar() }
                )

                DashboardUiState.Empty -> DashboardVacio(onNavegar = onNavegar)

                is DashboardUiState.Success -> ContenidoDashboard(
                    estado = actual,
                    onNavegar = onNavegar,
                    onActualizar = { viewModel.cargar() }
                )
            }
        }
    }
}

@Composable
private fun EncabezadoDashboard(
    sesion: Sesion,
    alertas: Int,
    onNotificaciones: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidGlass.VerdeNeon,
        borderBrush = Brush.linearGradient(
            listOf(
                LiquidGlass.VerdeNeon.copy(alpha = 0.55f),
                LiquidGlass.Azul.copy(alpha = 0.28f),
                LiquidGlass.Borde.copy(alpha = 0.18f)
            )
        ),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = saludoDelDia(),
                    style = MaterialTheme.typography.labelLarge,
                    color = LiquidGlass.VerdeNeon
                )
                Text(
                    text = sesion.usuario,
                    style = MaterialTheme.typography.headlineMedium,
                    color = LiquidGlass.Texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = LiquidGlass.TextoSuave,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = fechaLargaEspanol(),
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.TextoSuave
                    )
                }
                GlassPill(text = sesion.rol.etiqueta)
            }

            Box {
                GlassIconButton(
                    onClick = onNotificaciones,
                    content = {
                        Icon(
                            imageVector = if (alertas > 0) {
                                Icons.Default.Notifications
                            } else {
                                Icons.Default.NotificationsNone
                            },
                            contentDescription = "Notificaciones de inventario",
                            tint = if (alertas > 0) LiquidGlass.Ambar else LiquidGlass.TextoSuave
                        )
                    }
                )

                if (alertas > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(LiquidGlass.Peligro),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (alertas > 9) "9+" else alertas.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CargandoDashboard() {
    Reveal {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = LiquidGlass.Azul,
            contentPadding = PaddingValues(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassLoading()
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Cargando tu resumen…",
                        style = MaterialTheme.typography.titleSmall,
                        color = LiquidGlass.Texto
                    )
                    Text(
                        text = "Consultando ventas y productos reales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.TextoSuave
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorDashboard(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Reveal {
        GlassErrorState(message = mensaje)
    }
    Reveal(delayMillis = 80) {
        GlassButton(
            text = "Reintentar",
            onClick = onReintentar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DashboardVacio(
    onNavegar: (DestinoApp) -> Unit
) {
    Reveal {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = LiquidGlass.Azul,
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassIconTile(
                    icon = Icons.Default.Inventory2,
                    tint = LiquidGlass.Azul,
                    container = LiquidGlass.Azul.copy(alpha = 0.12f),
                    size = 48
                )
                Text(
                    text = "Aún no hay datos",
                    style = MaterialTheme.typography.titleMedium,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = "Registra productos y ventas para ver aquí tu resumen real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    textAlign = TextAlign.Center
                )
                GlassButton(
                    text = "Ir a productos",
                    onClick = { onNavegar(DestinoApp.PRODUCTOS) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ContenidoDashboard(
    estado: DashboardUiState.Success,
    onNavegar: (DestinoApp) -> Unit,
    onActualizar: () -> Unit
) {
    Reveal {
        GlassSectionHeader(
            titulo = "Resumen de ventas",
            detalle = "Ingresos reales de tu tienda",
            accion = {
                GlassIconButton(
                    onClick = onActualizar,
                    content = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar resumen",
                            tint = LiquidGlass.TextoSuave,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    Reveal(delayMillis = 60) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniMetrica(
                    titulo = "Ventas de hoy",
                    valor = formatearMontoResumen(estado.ventasHoy),
                    detalle = "Hoy",
                    icono = Icons.Default.Payments,
                    tono = LiquidGlass.VerdeNeon,
                    modifier = Modifier.weight(1f)
                )
                MiniMetrica(
                    titulo = "Ventas del mes",
                    valor = formatearMontoResumen(estado.ventasMes),
                    detalle = "Mes actual",
                    icono = Icons.AutoMirrored.Filled.TrendingUp,
                    tono = LiquidGlass.Azul,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniMetrica(
                    titulo = "Ticket promedio",
                    valor = formatearMontoResumen(estado.ticketPromedio),
                    detalle = "Por operación",
                    icono = Icons.AutoMirrored.Filled.ReceiptLong,
                    tono = LiquidGlass.Ambar,
                    modifier = Modifier.weight(1f)
                )
                MiniMetrica(
                    titulo = "Operaciones",
                    valor = estado.ventasValidas.toString(),
                    detalle = "Ventas válidas",
                    icono = Icons.Default.PointOfSale,
                    tono = LiquidGlass.Esmeralda,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Reveal(delayMillis = 120) {
        GraficoVentasCard(serie = estado.serieVentas)
    }

    Reveal(delayMillis = 180) {
        GlassSectionHeader(
            titulo = "Ventas recientes",
            detalle = "Últimos movimientos registrados",
            accion = {
                TextButton(onClick = { onNavegar(DestinoApp.HISTORIAL) }) {
                    Text(
                        text = "Ver historial",
                        style = MaterialTheme.typography.labelLarge,
                        color = LiquidGlass.VerdeNeon
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    if (estado.ultimasVentas.isEmpty()) {
        Reveal(delayMillis = 220) {
            GlassEmptyState(
                titulo = "Sin ventas recientes",
                message = "Cuando registres una venta aparecerá aquí con su monto real."
            )
        }
    } else {
        estado.ultimasVentas.forEachIndexed { indice, venta ->
            Reveal(delayMillis = 220 + indice * 40) {
                VentaRecienteRow(
                    venta = venta,
                    onClick = { onNavegar(DestinoApp.HISTORIAL) }
                )
            }
        }
    }

    Reveal(delayMillis = 420) {
        GlassSectionHeader(
            titulo = "Alertas de inventario",
            detalle = "Productos que requieren atención",
            accion = {
                TextButton(onClick = { onNavegar(DestinoApp.PRODUCTOS) }) {
                    Text(
                        text = "Ver productos",
                        style = MaterialTheme.typography.labelLarge,
                        color = LiquidGlass.VerdeNeon
                    )
                }
            },
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    Reveal(delayMillis = 460) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniMetrica(
                titulo = "Stock bajo",
                valor = estado.stockBajo.toString(),
                detalle = "1 a 5 unidades",
                icono = Icons.Default.WarningAmber,
                tono = LiquidGlass.Ambar,
                modifier = Modifier.weight(1f)
            )
            MiniMetrica(
                titulo = "Agotados",
                valor = estado.stockAgotado.toString(),
                detalle = "Sin unidades",
                icono = Icons.Default.ErrorOutline,
                tono = LiquidGlass.Peligro,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (estado.productosBajoStock.isEmpty() && estado.productosAgotados.isEmpty()) {
        Reveal(delayMillis = 500) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = LiquidGlass.VerdeNeon,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassIconTile(
                        icon = Icons.Default.CloudDone,
                        tint = LiquidGlass.VerdeNeon,
                        container = LiquidGlass.VerdeNeon.copy(alpha = 0.12f),
                        size = 42
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Inventario saludable",
                            style = MaterialTheme.typography.titleSmall,
                            color = LiquidGlass.Texto
                        )
                        Text(
                            text = "No hay productos con stock bajo ni agotados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave
                        )
                    }
                }
            }
        }
    } else {
        estado.productosBajoStock.take(4).forEachIndexed { indice, producto ->
            Reveal(delayMillis = 500 + indice * 35) {
                ProductoAlertaRow(
                    producto = producto,
                    agotado = false,
                    onClick = { onNavegar(DestinoApp.PRODUCTOS) }
                )
            }
        }

        estado.productosAgotados.take(4).forEachIndexed { indice, producto ->
            Reveal(delayMillis = 640 + indice * 35) {
                ProductoAlertaRow(
                    producto = producto,
                    agotado = true,
                    onClick = { onNavegar(DestinoApp.PRODUCTOS) }
                )
            }
        }
    }

    Reveal(delayMillis = 780) {
        GlassSectionHeader(
            titulo = "Accesos rápidos",
            detalle = "Continúa trabajando",
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }

    Reveal(delayMillis = 820) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AccesoRapido(
                titulo = "Nueva venta",
                icono = Icons.Default.PointOfSale,
                tono = LiquidGlass.Ambar,
                modifier = Modifier.weight(1f),
                onClick = { onNavegar(DestinoApp.NUEVA_VENTA) }
            )
            AccesoRapido(
                titulo = "Historial",
                icono = Icons.Default.History,
                tono = LiquidGlass.Azul,
                modifier = Modifier.weight(1f),
                onClick = { onNavegar(DestinoApp.HISTORIAL) }
            )
            AccesoRapido(
                titulo = "Reportes",
                icono = Icons.Default.QueryStats,
                tono = LiquidGlass.Esmeralda,
                modifier = Modifier.weight(1f),
                onClick = { onNavegar(DestinoApp.REPORTES) }
            )
        }
    }
}

@Composable
private fun MiniMetrica(
    titulo: String,
    valor: String,
    detalle: String,
    icono: ImageVector,
    tono: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        glowColor = tono,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GlassIconTile(
                    icon = icono,
                    tint = tono,
                    container = tono.copy(alpha = 0.12f),
                    size = 26
                )
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = LiquidGlass.TextoSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                color = LiquidGlass.Texto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.labelSmall,
                color = LiquidGlass.TextoSuave,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GraficoVentasCard(serie: List<PuntoVentaDiaria>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidGlass.VerdeNeon,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ventas por día",
                    style = MaterialTheme.typography.titleMedium,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = if (serie.isEmpty()) {
                        "Sin movimientos registrados"
                    } else {
                        "Total ${formatearMonto(serie.sumOf { it.total })} en ${serie.size} días con movimiento"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave
                )
            }
            GlassPill(
                text = "${serie.size} días",
                color = LiquidGlass.VerdeNeon,
                container = LiquidGlass.VerdeNeon.copy(alpha = 0.12f)
            )
        }

        Spacer(Modifier.height(14.dp))

        if (serie.isEmpty()) {
            Text(
                text = "Todavía no hay ventas válidas para graficar.",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.TextoSuave
            )
        } else {
            GraficoBarras(serie = serie)
        }
    }
}

@Composable
private fun GraficoBarras(serie: List<PuntoVentaDiaria>) {
    val maximo = serie.maxOf { it.total }.takeIf { it > 0.0 } ?: 1.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        serie.forEach { punto ->
            val proporcion = (punto.total / maximo).toFloat().coerceIn(0f, 1f)
            val alturaBarra = 12f + 92f * proporcion

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(alturaBarra.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 8.dp,
                                topEnd = 8.dp,
                                bottomStart = 3.dp,
                                bottomEnd = 3.dp
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    LiquidGlass.VerdeNeon,
                                    LiquidGlass.Azul.copy(alpha = 0.72f)
                                )
                            )
                        )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = punto.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidGlass.TextoSuave,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun VentaRecienteRow(
    venta: Venta,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        glowColor = LiquidGlass.Azul,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconTile(
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                tint = LiquidGlass.Azul,
                container = LiquidGlass.Azul.copy(alpha = 0.12f),
                size = 40
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Venta #${venta.id ?: "—"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto,
                    maxLines = 1
                )
                Text(
                    text = "${fechaCorta(venta.fecha)} · ${venta.tipoComprobante ?: "Comprobante"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatearMonto(venta.total ?: 0.0),
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto,
                    maxLines = 1
                )
                Text(
                    text = venta.estado ?: "EMITIDA",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidGlass.VerdeNeon,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProductoAlertaRow(
    producto: ProductoAlerta,
    agotado: Boolean,
    onClick: () -> Unit
) {
    val tono = if (agotado) LiquidGlass.Peligro else LiquidGlass.Ambar

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        glowColor = tono,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconTile(
                icon = if (agotado) Icons.Default.ErrorOutline else Icons.Default.WarningAmber,
                tint = tono,
                container = tono.copy(alpha = 0.12f),
                size = 38
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (agotado) {
                        "Sin unidades disponibles"
                    } else {
                        "Quedan ${producto.stock} unidades"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave
                )
            }

            GlassPill(
                text = if (agotado) "Agotado" else "${producto.stock} uds.",
                color = tono,
                container = tono.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    titulo: String,
    icono: ImageVector,
    tono: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick,
        glowColor = tono,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconTile(
                icon = icono,
                tint = tono,
                container = tono.copy(alpha = 0.12f),
                size = 38
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                color = LiquidGlass.Texto,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun saludoDelDia(): String = when {
    horaDelDia() < 12 -> "Buenos días"
    horaDelDia() < 19 -> "Buenas tardes"
    else -> "Buenas noches"
}

private fun fechaCorta(fecha: String?): String =
    fecha?.replace('T', ' ')?.take(16) ?: "Sin fecha"

private fun formatearMonto(valor: Double): String {
    val centavos = (valor * 100).roundToLong()
    val signo = if (centavos < 0) "-" else ""
    val absoluto = abs(centavos)
    val enteros = absoluto / 100
    val decimales = absoluto % 100
    return "S/ $signo${agruparEnteros(enteros)}.${decimales.toString().padStart(2, '0')}"
}

private fun formatearMontoResumen(valor: Double): String = when {
    abs(valor) >= 1_000_000 -> "S/ ${redondear1(valor / 1_000_000)}M"
    abs(valor) >= 10_000 -> "S/ ${redondear1(valor / 1_000)}k"
    else -> formatearMonto(valor)
}

private fun redondear1(valor: Double): String {
    val redondeado = (valor * 10).roundToLong() / 10.0
    return if (redondeado % 1.0 == 0.0) {
        redondeado.toLong().toString()
    } else {
        redondeado.toString()
    }
}

private fun agruparEnteros(valor: Long): String {
    val texto = valor.toString()
    if (texto.length <= 3) return texto
    return texto.reversed().chunked(3).joinToString(",").reversed()
}
