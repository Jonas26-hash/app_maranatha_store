package com.maranathastore.app.presentation.producto

import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.domain.producto.UMBRAL_STOCK_BAJO

data class ProductoUi(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: String,
    val precioTexto: String,
    val stock: Int,
    val activo: Boolean,
    val stockBajo: Boolean
) {
    val agotado: Boolean
        get() = stock <= 0
}

fun Producto.aUi(): ProductoUi = ProductoUi(
    id = id,
    codigo = codigo,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio.formateado(),
    precioTexto = precio.textoPlano(),
    stock = stock,
    activo = activo,
    stockBajo = stock <= UMBRAL_STOCK_BAJO
)