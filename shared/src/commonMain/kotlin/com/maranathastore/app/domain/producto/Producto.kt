package com.maranathastore.app.domain.producto

/**
 * Umbral visual de stock bajo. El frontend de Maranatha usa `<= 10`, por eso
 * se reproduce ese mismo criterio en la app móvil como indicador derivado.
 */
const val UMBRAL_STOCK_BAJO = 10

data class Producto(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Precio,
    val stock: Int,
    val activo: Boolean,
    val version: Long? = null
) {
    val stockBajo: Boolean
        get() = stock in 1..UMBRAL_STOCK_BAJO
}

data class DatosProducto(
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Precio,
    val stock: Int,
    val activo: Boolean
)