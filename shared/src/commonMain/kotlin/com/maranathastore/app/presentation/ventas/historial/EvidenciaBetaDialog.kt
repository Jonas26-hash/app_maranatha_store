package com.maranathastore.app.presentation.ventas.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maranathastore.app.data.remote.EvidenciaBetaDto
import com.maranathastore.app.data.remote.ResultadoLaboratorioDto
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.presentation.ventas.recordarImpresionComprobante
import com.maranathastore.app.theme.LiquidGlass
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

private fun evidenciaValida(v: EvidenciaBetaDto, numero: String): Boolean =
    v.ambiente == "BETA" && !v.validezFiscal && v.comprobante == "F9B1-$numero" &&
        v.estadoSunat == "ACEPTADO_BETA" && v.codigo == "0" && v.contenidoQr.isNotBlank()

private fun errorBeta(e: Exception, emision: Boolean): String {
    val status = (e as? ResponseException)?.response?.status?.value
    return when (status) {
        401, 403 -> "Se requiere una sesion de administrador."
        404 -> if (emision) "Ruta de emision no disponible. Revisa el despliegue del laboratorio." else "No hay evidencia archivada para ese correlativo F9B1."
        409 -> "Evidencia u operacion inconsistente. Revisa antes de repetir."
        503 -> "Laboratorio no disponible o SUNAT Beta deshabilitado."
        else -> if (emision) "Emision no confirmada. Consulta el historial de laboratorio antes de repetir. No generes otra operacion." else "No se pudo consultar la evidencia${status?.let { " (HTTP $it)" }.orEmpty()}."
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun EvidenciaBetaDialog(cargar: suspend (String) -> EvidenciaBetaDto, emitir: suspend (String) -> ResultadoLaboratorioDto, cerrar: () -> Unit) {
    var numero by rememberSaveable { mutableStateOf("00000010") }
    var operacion by rememberSaveable { mutableStateOf("") }
    var confirmar by remember { mutableStateOf(false) }
    var dato by remember { mutableStateOf<EvidenciaBetaDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    suspend fun consultar(solicitado: String) {
        val v = cargar(solicitado)
        if (!evidenciaValida(v, solicitado)) error = "Evidencia no confirmada: ${v.estadoSunat}."
        else dato = v
    }
    fun iniciarConsulta() {
        if (ocupado || !numero.matches(Regex("[0-9]{8}"))) return
        val solicitado = numero
        ocupado = true; dato = null; error = null
        scope.launch {
            try { consultar(solicitado) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = errorBeta(e, false) }
            finally { ocupado = false }
        }
    }
    // Consulta de lectura solamente; no genera una nueva factura.
    LaunchedEffect(Unit) { iniciarConsulta() }
    AlertDialog(onDismissRequest = { if (!ocupado) cerrar() }, containerColor = LiquidGlass.SuperficieDialogo,
        title = { Text("Laboratorio SUNAT Beta", color = LiquidGlass.Texto) },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Facturas sinteticas F9B1. Sin validez fiscal. Separadas de las ventas comerciales.", color = LiquidGlass.TextoSuave)
                OutlinedTextField(value = numero, onValueChange = { if (!ocupado) { numero = it.filter { c -> c in '0'..'9' }.take(8); dato = null; error = null } }, label = { Text("Correlativo F9B1") }, singleLine = true, enabled = !ocupado)
                Button(enabled = !ocupado && numero.length == 8, onClick = { iniciarConsulta() }) { Text("Consultar evidencia") }
                Button(enabled = !ocupado && operacion.isEmpty(), onClick = { confirmar = true }) { Text("Generar factura de prueba Beta") }
                if (ocupado) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Procesando. No cierres la pantalla ni repitas la operacion.", color = LiquidGlass.TextoSuave) }
                if (operacion.isNotEmpty() && !ocupado) Text("Operacion pendiente de revision. No se habilita otra emision en esta pantalla.", color = LiquidGlass.TextoSuave)
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
        }, confirmButton = { TextButton(onClick = cerrar, enabled = !ocupado) { Text("Cerrar") } })
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("Emitir factura sintetica Beta") },
        text = { Text("Se enviaran exclusivamente datos sinteticos al ambiente de pruebas. No modifica ventas comerciales ni inventario.") },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        confirmButton = { TextButton(onClick = {
            confirmar = false; ocupado = true; dato = null; error = null
            operacion = Uuid.random().toString()
            scope.launch {
                try {
                    val r = emitir(operacion)
                    if (r.ambiente != "BETA" || r.validezFiscal || r.serie != "F9B1" || !r.numero.matches(Regex("[0-9]{8}"))) {
                        error = "Respuesta inconsistente. Revisa la operacion."
                    } else {
                        numero = r.numero
                        if (r.estadoSunat == "ACEPTADO_BETA" && r.codigo == "0") {
                            consultar(numero)
                            if (dato != null) operacion = ""
                        } else error = "Resultado: F9B1-$numero · ${r.estadoSunat}. Requiere revision."
                    }
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { error = errorBeta(e, true) }
                finally { ocupado = false }
            }
        }) { Text("Confirmar") } })
}

@Composable
expect fun QrEvidenciaBeta(contenido: String)
