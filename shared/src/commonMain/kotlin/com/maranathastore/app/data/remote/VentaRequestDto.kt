package com.maranathastore.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class VentaRequestDto(
    val clienteId: Long?,
    val tipoComprobante: String,
    val detalles: List<DetalleVentaRequestDto>
)

@Serializable
data class DetalleVentaRequestDto(
    val productoId: Long,
    val cantidad: Int
)