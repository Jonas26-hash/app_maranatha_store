package com.maranathastore.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class VentaRequestDto(
    val clienteId: Long?,
    val tipoComprobante: String,
    val detalles: List<DetalleVentaRequestDto>,
    val idempotencyKey: String,
    val tipoDocumento: String? = null,
    val numeroDocumento: String? = null,
    val clienteNombre: String? = null,
    val metodoPago: String? = null
)

@Serializable
data class DetalleVentaRequestDto(
    val productoId: Long,
    val cantidad: Int
)
