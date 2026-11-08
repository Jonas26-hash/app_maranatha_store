package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class ProductoApi(
    private val client: HttpClient
) {

    suspend fun listar(): List<ProductoDto> =
        client.get("productos").body()

    suspend fun obtener(id: Long): ProductoDto =
        client.get("productos/$id").body()

    suspend fun crear(dto: ProductoRequestDto): ProductoDto =
        client.post("productos") { setBody(dto) }.body()

    suspend fun actualizar(id: Long, dto: ProductoRequestDto): ProductoDto =
        client.put("productos/$id") { setBody(dto) }.body()

    suspend fun eliminar(id: Long) {
        client.delete("productos/$id")
    }

    suspend fun reponerStock(id: Long, cantidad: Int): ProductoDto =
        client.post("productos/$id/stock/reponer") {
            setBody(StockRequestDto(cantidad))
        }.body()

    suspend fun descontarStock(id: Long, cantidad: Int): ProductoDto =
        client.post("productos/$id/stock/descontar") {
            setBody(StockRequestDto(cantidad))
        }.body()
}