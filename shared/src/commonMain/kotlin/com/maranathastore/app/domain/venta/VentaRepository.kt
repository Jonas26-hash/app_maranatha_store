package com.maranathastore.app.domain.venta

import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.VentaDto

interface VentaRepository {
    suspend fun listar(): List<Venta>
}

data class Venta(
    val id: Long?,
    val fecha: String?,
    val total: Double?,
    val clienteId: Long?,
    val tipoComprobante: String?,
    val estado: String?
)

class VentaRepositorioRemoto(private val api: VentaApi) : VentaRepository {
    override suspend fun listar(): List<Venta> =
        api.listar().map { it.toDomain() }
}

fun VentaDto.toDomain(): Venta = Venta(
    id = id,
    fecha = fecha,
    total = total,
    clienteId = clienteId,
    tipoComprobante = tipoComprobante,
    estado = estado
)