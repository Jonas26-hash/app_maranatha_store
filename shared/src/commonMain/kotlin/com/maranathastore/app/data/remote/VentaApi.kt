package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class VentaDetalleDto(
    val productoId: Long? = null,
    val cantidad: Int? = null,
    val precioUnitario: Double? = null,
    val subtotal: Double? = null
)

@Serializable
data class VentaDto(
    val id: Long? = null,
    val fecha: String? = null,
    val total: Double? = null,
    val clienteId: Long? = null,
    val tipoComprobante: String? = null,
    val estado: String? = null,
    val detalles: List<VentaDetalleDto>? = null
)

class VentaApi(private val client: HttpClient) {
    suspend fun listar(): List<VentaDto> = client.get("ventas").body()
    suspend fun obtener(id: Long): VentaDto = client.get("ventas/$id").body()
    suspend fun crear(dto: VentaRequestDto): VentaDto = client.post("ventas") { setBody(dto) }.body()
    suspend fun anular(id: Long): VentaDto = client.put("ventas/$id/anular").body()
}