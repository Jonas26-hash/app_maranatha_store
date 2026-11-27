package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.plugins.timeout
import kotlinx.serialization.Serializable

@Serializable
data class ConsultaRucDto(val ruc: String, val razonSocial: String, val estado: String? = null, val condicion: String? = null, val direccionFiscal: String? = null)

class FacturacionApi(private val client: HttpClient) {
    suspend fun consultarDni(dni: String): ConsultaDniDto {
        require(dni.matches(Regex("[0-9]{8}")))
        return client.get("clientes/documentos/dni/$dni") { timeout { requestTimeoutMillis = 25000; socketTimeoutMillis = 22000 } }.body()
    }
    suspend fun consultarRuc(ruc: String): ConsultaRucDto {
        require(ruc.matches(Regex("[0-9]{11}")))
        return client.get("api/facturacion/ruc/$ruc") { timeout { requestTimeoutMillis = 25000; socketTimeoutMillis = 22000 } }.body()
    }
}

@Serializable
data class ConsultaDniDto(val numeroDocumento: String, val nombres: String, val apellidoPaterno: String = "", val apellidoMaterno: String = "")
