package com.maranathastore.app.presentation.ventas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maranathastore.app.data.remote.*
import kotlinx.coroutines.CancellationException
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun SelectorComprobante(venta: VentaDto, onCerrar: () -> Unit,
    cargarEstado: (suspend (Long) -> EstadoFiscalDto)? = null,
    cargarComprobante: (suspend (Long) -> ComprobanteLocalDto)? = null) {
    var fiscal by remember(venta.id) { mutableStateOf<EstadoFiscalDto?>(null) }
    var local by remember(venta.id) { mutableStateOf<ComprobanteLocalDto?>(null) }
    var aviso by remember(venta.id) { mutableStateOf<String?>(null) }
    LaunchedEffect(venta.id) {
        val id = venta.id ?: return@LaunchedEffect
        try { fiscal = cargarEstado?.invoke(id)?.takeIf { it.ventaId == id && it.serie == venta.serie && it.numero == venta.numero } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { aviso = "No se pudo consultar el estado actualizado." }
        if (venta.tipoComprobante in listOf("FACTURA", "BOLETA") && venta.estado == "EMITIDA") {
            try { local = cargarComprobante?.invoke(id)?.takeIf { it.serie == venta.serie && it.numero == venta.numero } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { aviso = "Datos fiscales no disponibles o acceso restringido." }
        }
    }
    var formato by remember { mutableStateOf<String?>(null) }
    val imprimir = recordarImpresionComprobante(venta, formato ?: "A4", local?.takeIf { !it.validezFiscal && it.ambiente == "BORRADOR_LOCAL" })
    AlertDialog(onDismissRequest = onCerrar,
        containerColor = LiquidGlass.SuperficieDialogo,
        title = { Text(if (formato == null) "Formato de comprobante" else "Vista previa · $formato", color = LiquidGlass.Texto) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (formato == null) {
                    Button(onClick = { formato = "TICKET" }, modifier = Modifier.fillMaxWidth()) { Text("Ticket 80 mm") }
                    Button(onClick = { formato = "A4" }, modifier = Modifier.fillMaxWidth()) { Text("Hoja A4") }
                } else {
                    Text("MARANATHA STORE", color = LiquidGlass.Texto)
                    Text("${venta.tipoComprobante.orEmpty()} ${venta.serie.orEmpty()}-${venta.numero.orEmpty()}", color = LiquidGlass.VerdeNeon)
                    Text(venta.fecha.orEmpty(), color = LiquidGlass.TextoSuave)
                    Text(venta.clienteNombre ?: "Comprador no identificado", color = LiquidGlass.Texto)
                    venta.clienteDocumento?.let { Text(it, color = LiquidGlass.TextoSuave) }
                    HorizontalDivider()
                    venta.detalles.orEmpty().forEach { Text("${it.cantidad ?: 0} x ${it.productoNombre ?: "Producto #${it.productoId}"} · S/ ${it.subtotal ?: 0.0}", color = LiquidGlass.Texto) }
                    Text("Total S/ ${venta.total ?: 0.0}", color = LiquidGlass.VerdeNeon)
                    Text("SUNAT: ${fiscal?.etiqueta ?: venta.etiquetaFiscal()}", color = LiquidGlass.TextoSuave)
                    local?.let {
                        Text("Gravado: S/ ${it.gravado} · IGV: S/ ${it.igv}", color = LiquidGlass.Texto)
                        Text("Exonerado: S/ ${it.exonerado} · Inafecto: S/ ${it.inafecto}", color = LiquidGlass.Texto)
                    }
                    aviso?.let { Text(it, color = LiquidGlass.TextoSuave) }
                    Text("Registro interno. No acredita validación SUNAT. QR fiscal no disponible.", color = LiquidGlass.TextoSuave)
                }
            }
        }, confirmButton = { if (formato != null && imprimir != null) Button(onClick = imprimir) { Text(if (formato == "TICKET") "Guardar Ticket / PDF" else "Imprimir / PDF") } else TextButton(onClick = onCerrar) { Text("Cerrar") } },
        dismissButton = { if (formato != null) TextButton(onClick = { formato = null }) { Text("Formato") } else TextButton(onClick = onCerrar) { Text("Ahora no") } })
}

@Composable
expect fun recordarImpresionComprobante(venta: VentaDto, formato: String, local: ComprobanteLocalDto? = null, beta: EvidenciaBetaDto? = null): (() -> Unit)?
