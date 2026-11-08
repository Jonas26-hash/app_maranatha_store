package com.maranathastore.app.data.remote

import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val jsonDelError = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * El backend Maranatha responde errores con la forma:
 * `{"timestamp": "...", "status": 400, "message": "..."}`.
 * No existe un mapa `validationErrors`; por eso no se inventa.
 */
@Serializable
private data class ErrorResponseDto(
    val message: String? = null,
    val status: Int? = null,
    val error: String? = null
)

internal suspend fun <T> alAplicacion(bloque: suspend () -> T): T = try {
    bloque()
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (fallo: Throwable) {
    throw ErrorDeApi(fallo.aErrorApi())
}

internal suspend fun Throwable.aErrorApi(): ErrorApi = when (this) {

    is HttpRequestTimeoutException -> ErrorApi.TiempoAgotado(
        "La solicitud tardó demasiado tiempo. Inténtalo de nuevo."
    )

    is ConnectTimeoutException -> ErrorApi.SinConexion(
        "No se pudo conectar con el servidor. Verifica tu conexión."
    )

    is SocketTimeoutException -> ErrorApi.TiempoAgotado(
        "El servidor tardó demasiado en responder. Inténtalo de nuevo."
    )

    is ResponseException -> aErrorApiDesdeRespuesta()

    is IOException -> ErrorApi.SinConexion(
        "No se pudo conectar con el servidor. Verifica tu conexión."
    )

    else -> ErrorApi.Servidor(
        message ?: "Se produjo un error inesperado.",
        codigo = null
    )
}

private suspend fun ResponseException.aErrorApiDesdeRespuesta(): ErrorApi {

    val codigo = response.status.value
    val cuerpo = leerCuerpoDeError()
    val mensaje = cuerpo?.message?.takeIf { it.isNotBlank() }
        ?: mensajePorDefecto(codigo)

    return when (codigo) {

        HttpStatusCode.BadRequest.value,
        HttpStatusCode.UnprocessableEntity.value ->
            ErrorApi.Validacion(mensaje)

        HttpStatusCode.NotFound.value ->
            ErrorApi.NoEncontrado(mensaje)

        HttpStatusCode.Unauthorized.value ->
            ErrorApi.NoAutorizado(mensaje)

        HttpStatusCode.Forbidden.value ->
            ErrorApi.Prohibido(mensaje)

        HttpStatusCode.Conflict.value ->
            ErrorApi.Conflicto(mensaje)

        else -> if (codigo >= 500) {
            ErrorApi.Servidor(mensaje, codigo)
        } else {
            ErrorApi.Validacion(mensaje)
        }
    }
}

private suspend fun ResponseException.leerCuerpoDeError(): ErrorResponseDto? = try {
    jsonDelError.decodeFromString<ErrorResponseDto>(response.bodyAsText())
} catch (_: Exception) {
    null
}

private fun mensajePorDefecto(codigo: Int): String = when {
    codigo == HttpStatusCode.BadRequest.value ||
        codigo == HttpStatusCode.UnprocessableEntity.value ->
        "El servidor rechazó los datos enviados."

    codigo == HttpStatusCode.NotFound.value ->
        "No se encontró el recurso solicitado."

    codigo == HttpStatusCode.Conflict.value ->
        "La operación entra en conflicto con el estado actual."

    codigo == HttpStatusCode.Unauthorized.value ->
        "No autorizado. La sesión no es válida o las credenciales son incorrectas."

    codigo == HttpStatusCode.Forbidden.value ->
        "No tienes permisos para realizar esta operación."

    codigo >= 500 ->
        "El servidor no pudo completar la operación. Inténtalo más tarde."

    else -> "El servidor devolvió un error ($codigo)."
}
