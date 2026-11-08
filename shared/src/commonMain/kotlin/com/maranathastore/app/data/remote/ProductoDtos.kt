package com.maranathastore.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Long,
    val codigo: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true,
    val version: Long? = null
)

@Serializable
data class ProductoRequestDto(
    val codigo: String? = null,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true
)

@Serializable
data class StockRequestDto(
    val cantidad: Int
)