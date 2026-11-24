package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class VentaDetalleDto(
    val productoId: Long? = null,
    val cantidad: Int? = null,
    val precioUnitario: Double? = null,
    val subtotal: Double? = null,
    val productoNombre: String? = null,
    val afectacionIgv: String? = null
)

@Serializable
data class VentaDto(
    val id: Long? = null,
    val fecha: String? = null,
    val total: Double? = null,
    val clienteId: Long? = null,
    val tipoComprobante: String? = null,
    val estado: String? = null,
    val detalles: List<VentaDetalleDto>? = null,
    val serie: String? = null,
    val numero: String? = null,
    val clienteNombre: String? = null,
    val clienteDocumento: String? = null,
    val estadoSunat: String? = null,
    val estadoPago: String? = null,
    val metodoPago: String? = null
)

class VentaApi(private val client: HttpClient) {
    suspend fun evidenciaBeta(numero: String): EvidenciaBetaDto = client.get("facturacion/laboratorio/comprobantes/$numero").body()
    suspend fun estadoFiscal(id: Long): EstadoFiscalDto = client.get("ventas/$id/estado-sunat").body()
    suspend fun comprobanteLocal(id: Long): ComprobanteLocalDto = client.get("ventas/$id/comprobante-local").body()
    suspend fun listar(): List<VentaDto> = client.get("ventas").body()
    suspend fun obtener(id: Long): VentaDto = client.get("ventas/$id").body()
    suspend fun crear(dto: VentaRequestDto): VentaDto = client.post("ventas") { header("Idempotency-Key", dto.idempotencyKey); setBody(dto) }.body()
    suspend fun anular(id: Long): VentaDto = client.put("ventas/$id/anular").body()
}

@Serializable
data class EstadoFiscalDto(val ventaId: Long, val serie: String, val numero: String, val estadoSunat: String, val etiqueta: String, val ambiente: String, val validezFiscalConfirmada: Boolean = false)
@Serializable
data class ComprobanteLocalDto(val ambiente: String, val validezFiscal: Boolean, val serie: String, val numero: String, val gravado: Double, val exonerado: Double, val inafecto: Double, val igv: Double, val total: Double, val contenidoQr: String? = null)

fun VentaDto.etiquetaFiscal(): String = if (tipoComprobante == "NOTA_PEDIDO") "No aplica" else when (estadoSunat) {
    "NO_APLICA" -> "No aplica"
    "PENDIENTE" -> "Pendiente"
    "EN_PROCESO" -> "En proceso"
    "ACEPTADO_BETA" -> "Aceptado en Beta"
    "OBSERVADO_BETA" -> "Observado en Beta"
    "RECHAZADO_BETA" -> "Rechazado en Beta"
    "ERROR_COMUNICACION" -> "Error de comunicación"
    "REQUIERE_REVISION" -> "Requiere revisión"
    "ACEPTADO" -> "Aceptado registrado (sin confirmación)"
    "OBSERVADO" -> "Observado registrado"
    "RECHAZADO" -> "Rechazado registrado"
    else -> "Sin registro fiscal"
}

@Serializable
data class EvidenciaBetaDto(val ambiente: String, val validezFiscal: Boolean, val comprobante: String, val estadoSunat: String, val codigo: String, val igv: String, val total: String, val fecha: String, val contenidoQr: String)
