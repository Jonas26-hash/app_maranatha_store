package com.maranathastore.app.presentation.ventas.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maranathastore.app.data.remote.EvidenciaBetaDto
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.presentation.ventas.recordarImpresionComprobante
import com.maranathastore.app.theme.LiquidGlass
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun EvidenciaBetaDialog(cargar: suspend (String) -> EvidenciaBetaDto, cerrar: () -> Unit) {
    var numero by remember { mutableStateOf("00000010") }
    var dato by remember { mutableStateOf<EvidenciaBetaDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = cerrar, containerColor = LiquidGlass.SuperficieDialogo,
        title = { Text("Laboratorio SUNAT Beta", color = LiquidGlass.Texto) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Solo evidencia archivada. Sin validez fiscal. No modifica ventas ni inventario.", color = LiquidGlass.TextoSuave)
                OutlinedTextField(value = numero, onValueChange = { if (!ocupado) { numero = it.filter { c -> c in '0'..'9' }.take(8); dato = null; error = null } }, label = { Text("Correlativo F9B1") }, singleLine = true, enabled = !ocupado)
                Button(enabled = !ocupado && numero.length == 8, onClick = {
                    val solicitado = numero
                    ocupado = true; dato = null; error = null
                    scope.launch {
                        try {
                            val v = cargar(solicitado)
                            if (v.ambiente != "BETA" || v.validezFiscal || v.comprobante != "F9B1-$solicitado" || v.estadoSunat != "ACEPTADO_BETA" || v.codigo != "0" || v.contenidoQr.isBlank()) error = "Evidencia no confirmada."
                            else dato = v
                        } catch (e: CancellationException) { throw e }
                        catch (_: Exception) { error = "No se pudo consultar la evidencia. Revisa el correlativo y el acceso de administrador." }
                        finally { ocupado = false }
                    }
                }) { Text(if (ocupado) "Consultando…" else "Consultar evidencia") }
                error?.let { Text(it, color = LiquidGlass.Peligro) }
                dato?.let { v ->
                    Text(v.comprobante, color = LiquidGlass.Texto)
                    Text("SUNAT: Aceptado en Beta · Código ${v.codigo}", color = LiquidGlass.VerdeNeon)
                    Text("Fecha: ${v.fecha} · IGV S/ ${v.igv} · Total S/ ${v.total}", color = LiquidGlass.Texto)
                    QrEvidenciaBeta(v.contenidoQr)
                    val referencia = VentaDto(serie = v.comprobante.substringBefore('-'), numero = v.comprobante.substringAfter('-'))
                    val ticket = recordarImpresionComprobante(referencia, "TICKET", beta = v)
                    val a4 = recordarImpresionComprobante(referencia, "A4", beta = v)
                    if (ticket != null && a4 != null) {
                        Button(onClick = ticket, modifier = Modifier.fillMaxWidth()) { Text("Guardar Ticket 80 mm / PDF") }
                        Button(onClick = a4, modifier = Modifier.fillMaxWidth()) { Text("Imprimir A4 / PDF") }
                    }
                    Text("QR de prueba · Sin validez fiscal", color = LiquidGlass.TextoSuave)
                }
            }
        }, confirmButton = { TextButton(onClick = cerrar) { Text("Cerrar") } })
}

@Composable
expect fun QrEvidenciaBeta(contenido: String)
