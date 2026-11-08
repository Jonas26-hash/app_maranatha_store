package com.maranathastore.app.data.remote

import com.maranathastore.app.domain.producto.DatosProducto
import com.maranathastore.app.domain.producto.Precio
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.domain.producto.ProductoRepository

class ProductoRepositorioRemoto(
    private val api: ProductoApi
) : ProductoRepository {

    override suspend fun listar(): List<Producto> = alAplicacion {
        api.listar().map { it.aDominio() }
    }

    override suspend fun obtener(id: Long): Producto = alAplicacion {
        api.obtener(id).aDominio()
    }

    override suspend fun crear(datos: DatosProducto): Producto = alAplicacion {
        api.crear(datos.aRequest()).aDominio()
    }

    override suspend fun actualizar(id: Long, datos: DatosProducto): Producto = alAplicacion {
        api.actualizar(id, datos.aRequest()).aDominio()
    }

    override suspend fun eliminar(id: Long): Unit = alAplicacion {
        api.eliminar(id)
    }

    override suspend fun reponerStock(id: Long, cantidad: Int): Producto = alAplicacion {
        api.reponerStock(id, cantidad).aDominio()
    }

    override suspend fun descontarStock(id: Long, cantidad: Int): Producto = alAplicacion {
        api.descontarStock(id, cantidad).aDominio()
    }

    private fun ProductoDto.aDominio(): Producto {
        val (cod, desc) = desempaquetar(this)
        return Producto(
            id = id,
            codigo = cod,
            nombre = nombre,
            descripcion = desc,
            precio = Precio.desdeDouble(precio),
            stock = stock,
            activo = activo,
            version = version
        )
    }

    private fun DatosProducto.aRequest(): ProductoRequestDto = ProductoRequestDto(
        codigo = codigo.trim().uppercase(),
        nombre = nombre,
        descripcion = empaquetarDescripcion(codigo, descripcion),
        precio = precio.comoDouble,
        stock = stock,
        activo = activo
    )

    private companion object {
        val REGEX_CODIGO = Regex("""^\[([A-Za-z0-9\-_./]+)\]\s*(.*)$""", RegexOption.DOT_MATCHES_ALL)

        fun empaquetarDescripcion(codigo: String, descripcion: String?): String {
            val cod = codigo.trim().uppercase()
            val desc = descripcion?.trim().orEmpty()
            return if (desc.isBlank()) "[$cod]" else "[$cod] $desc"
        }

        fun desempaquetar(dto: ProductoDto): Pair<String, String?> {
            if (!dto.codigo.isNullOrBlank()) {
                return dto.codigo.trim().uppercase() to dto.descripcion?.trim()?.ifBlank { null }
            }
            val descRaw = dto.descripcion?.trim()
            if (!descRaw.isNullOrBlank()) {
                val match = REGEX_CODIGO.find(descRaw)
                if (match != null) {
                    val cod = match.groupValues[1].uppercase()
                    val descReal = match.groupValues[2].trim().ifBlank { null }
                    return cod to descReal
                }
            }
            return "ART-${dto.id}" to dto.descripcion?.trim()?.ifBlank { null }
        }
    }
}