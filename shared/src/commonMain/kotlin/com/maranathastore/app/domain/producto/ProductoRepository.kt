package com.maranathastore.app.domain.producto

interface ProductoRepository {
    suspend fun listar(): List<Producto>
    suspend fun obtener(id: Long): Producto
    suspend fun crear(datos: DatosProducto): Producto
    suspend fun actualizar(id: Long, datos: DatosProducto): Producto
    suspend fun eliminar(id: Long)
    suspend fun reponerStock(id: Long, cantidad: Int): Producto
    suspend fun descontarStock(id: Long, cantidad: Int): Producto
}