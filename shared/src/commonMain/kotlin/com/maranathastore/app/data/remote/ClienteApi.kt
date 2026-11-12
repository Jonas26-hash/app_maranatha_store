package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: Long? = null,
    val nombre: String? = null,
    val apellido: String? = null,
    val tipoDocumento: String? = null,
    val numeroDocumento: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val activo: Boolean? = null
)

@Serializable
data class ClienteRequestDto(
    val nombre: String,
    val apellido: String? = null,
    val tipoDocumento: String,
    val numeroDocumento: String,
    val email: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

class ClienteApi(private val client: HttpClient) {
    suspend fun listar(): List<ClienteDto> = client.get("clientes").body()
    suspend fun crear(dto: ClienteRequestDto): ClienteDto = client.post("clientes") { setBody(dto) }.body()
    suspend fun actualizar(id: Long, dto: ClienteRequestDto): ClienteDto =
        client.put("clientes/$id") { setBody(dto) }.body()
    suspend fun eliminar(id: Long) {
        client.delete("clientes/$id")
    }
}