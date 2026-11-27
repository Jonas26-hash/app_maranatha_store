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
    cargarComprobante: (suspend (Long) -> ComprobanteLocalDto)? = null,
    cargarBeta: (suspend (VentaDto) -> EvidenciaBetaDto)? = null,
    nuevaVenta: Boolean = false) {
    var fiscal by remember(venta.id, venta.estado, venta.estadoSunat) { mutableStateOf<EstadoFiscalDto?>(null) }
    var local by remember(venta.id, venta.estado, venta.estadoSunat) { mutableStateOf<ComprobanteLocalDto?>(null) }
    var beta by remember(venta.id, venta.estado, venta.estadoSunat) { mutableStateOf<EvidenciaBetaDto?>(null) }
    val requiereBeta = venta.estadoSunat == "ACEPTADO_BETA" && venta.estado != "ANULADA"
    var aviso by remember(venta.id, venta.estado, venta.estadoSunat) { mutableStateOf<String?>(null) }
    var intento by remember(venta.id) { mutableStateOf(0) }
    LaunchedEffect(venta.id, venta.estado, venta.estadoSunat, intento) {
        aviso = null
        val id = venta.id ?: return@LaunchedEffect
        try { fiscal = cargarEstado?.invoke(id)?.takeIf { it.ventaId == id && it.serie == venta.serie && it.numero == venta.numero } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { aviso = "No se pudo consultar el estado actualizado." }
        if (requiereBeta) {
            try { beta = cargarBeta?.invoke(venta) }
            catch(e: CancellationException) { throw e }
            catch(_: Exception) { aviso = "Evidencia fiscal no verificable. No se permite imprimir como aceptada." }
        } else if (venta.tipoComprobante in listOf("FACTURA", "BOLETA") && venta.estado == "EMITIDA") {
            try { local = cargarComprobante?.invoke(id)?.takeIf { it.serie == venta.serie && it.numero == venta.numero } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { aviso = "Datos fiscales no disponibles o acceso restringido." }
        }
    }
    var formato by remember { mutableStateOf<String?>(null) }
    val accionImprimir = recordarImpresionComprobante(venta, formato ?: "A4", local?.takeIf { !it.validezFiscal && it.ambiente == "BORRADOR_LOCAL" }, beta)
    val imprimir = accionImprimir?.takeIf { !requiereBeta || beta != null }
    val compartir = recordarCompartirComprobante(venta, local, beta)?.takeIf { !requiereBeta || beta != null }
    AlertDialog(onDismissRequest = onCerrar,
        containerColor = LiquidGlass.SuperficieDialogo,
        title = { Text(if (formato == null) (if(nuevaVenta) "Venta realizada" else "Formato de comprobante") else "Vista previa · $formato", color = LiquidGlass.Texto) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (formato == null) {
                    Text("Formato de comprobante", color = LiquidGlass.Texto)
                    Text("SUNAT: ${venta.etiquetaFiscal()}", color = LiquidGlass.TextoSuave)
                    venta.compradorLocalNombre?.let { Text("Comprador local: $it · ${venta.compradorLocalDocumento.orEmpty()}", color = LiquidGlass.Texto) }
                    if (requiereBeta && beta == null) Text(aviso ?: "Verificando XML y CDR...", color = LiquidGlass.TextoSuave)
                    if (requiereBeta && beta == null && aviso != null) TextButton(onClick = { intento++ }) { Text("Reintentar verificación") }
                    Button(onClick = { formato = "TICKET" }, enabled = !requiereBeta || beta != null, modifier = Modifier.fillMaxWidth()) { Text("Ticket 80 mm") }
                    Button(onClick = { formato = "A4" }, enabled = !requiereBeta || beta != null, modifier = Modifier.fillMaxWidth()) { Text("Hoja A4") }
                    Text("Compartir comprobante", color = LiquidGlass.Texto)
                    Button(onClick = { compartir?.invoke() }, enabled = compartir != null, modifier = Modifier.fillMaxWidth()) { Text("Compartir PDF (WhatsApp / correo)") }
                } else {
                    Text("MARANATHA STORE", color = LiquidGlass.Texto)
                    Text("${venta.tipoComprobante.orEmpty()} ${venta.serie.orEmpty()}-${venta.numero.orEmpty()}", color = LiquidGlass.VerdeNeon)
                    Text(venta.fecha.orEmpty(), color = LiquidGlass.TextoSuave)
                    venta.compradorLocalNombre?.let { Text("Comprador local (no fiscal): $it", color = LiquidGlass.Texto); Text(venta.compradorLocalDocumento.orEmpty(), color = LiquidGlass.TextoSuave) }
                    Text("Cliente: " + (venta.clienteNombre ?: "Comprador no identificado"), color = LiquidGlass.Texto)
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
                    if(beta != null) {
                        Text("IGV: S/ ${beta!!.igv} · Total: S/ ${beta!!.total}", color = LiquidGlass.Texto)
                        com.maranathastore.app.presentation.ventas.historial.QrEvidenciaBeta(beta!!.contenidoQr)
                        Text("Referencia Beta: ${beta!!.comprobante} · XML firmado · CDR código 0 · Sin validez fiscal", color = LiquidGlass.TextoSuave)
                    } else Text(if (requiereBeta) "QR fiscal pendiente de verificar" else "Registro interno. No acredita validación SUNAT.", color = LiquidGlass.TextoSuave)
                }
            }
        }, confirmButton = { if (formato != null && imprimir != null) Button(onClick = imprimir) { Text(if (formato == "TICKET") "Guardar Ticket / PDF" else "Imprimir / PDF") } else TextButton(onClick = onCerrar) { Text(if(nuevaVenta) "Nueva venta" else "Cerrar") } },
        dismissButton = { if (formato != null) TextButton(onClick = { formato = null }) { Text("Formato") } else TextButton(onClick = onCerrar) { Text("Ahora no") } })
}

@Composable
expect fun recordarImpresionComprobante(venta: VentaDto, formato: String, local: ComprobanteLocalDto? = null, beta: EvidenciaBetaDto? = null): (() -> Unit)?

@Composable
expect fun recordarCompartirComprobante(venta: VentaDto, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): (() -> Unit)?
