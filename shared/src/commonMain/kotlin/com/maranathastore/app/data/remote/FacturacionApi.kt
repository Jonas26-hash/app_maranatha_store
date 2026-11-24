package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

@Serializable
data class ConsultaRucDto(val ruc: String, val razonSocial: String, val estado: String? = null, val condicion: String? = null, val direccionFiscal: String? = null)

class FacturacionApi(private val client: HttpClient) {
    suspend fun consultarRuc(ruc: String): ConsultaRucDto {
        require(ruc.matches(Regex("[0-9]{11}")))
        return alAplicacion { client.get("facturacion/ruc/$ruc").body() }
    }
}
