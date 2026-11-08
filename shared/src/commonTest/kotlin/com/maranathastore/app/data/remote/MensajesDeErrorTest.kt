package com.maranathastore.app.data.remote

import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.support.responderJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class MensajesDeErrorTest {

    @Test
    fun `400 se traduce a error de validacion con el mensaje del backend`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.BadRequest,
            """{"timestamp":"2026-01-01T00:00:00","status":400,"message":"Datos inválidos"}"""
        )

        assertEquals(ErrorApi.Validacion("Datos inválidos"), error)
    }

    @Test
    fun `404 se traduce a no encontrado`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.NotFound,
            """{"status":404,"message":"Producto no encontrado"}"""
        )

        assertEquals(ErrorApi.NoEncontrado("Producto no encontrado"), error)
    }

    @Test
    fun `401 se traduce a no autorizado`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.Unauthorized,
            """{"status":401,"message":"Token inválido"}"""
        )

        assertEquals(ErrorApi.NoAutorizado("Token inválido"), error)
    }

    @Test
    fun `403 se traduce a prohibido`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.Forbidden,
            """{"status":403,"message":"Sin permisos"}"""
        )

        assertEquals(ErrorApi.Prohibido("Sin permisos"), error)
    }

    @Test
    fun `409 se traduce a conflicto`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.Conflict,
            """{"status":409,"message":"Stock insuficiente"}"""
        )

        assertEquals(ErrorApi.Conflicto("Stock insuficiente"), error)
    }

    @Test
    fun `500 conserva el codigo y el mensaje del backend`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.InternalServerError,
            """{"status":500,"message":"Fallo interno"}"""
        )

        assertEquals(ErrorApi.Servidor("Fallo interno", 500), error)
    }

    @Test
    fun `422 se traduce a validacion`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.UnprocessableEntity,
            """{"status":422,"message":"Regla de negocio incumplida"}"""
        )

        assertEquals(ErrorApi.Validacion("Regla de negocio incumplida"), error)
    }

    @Test
    fun `418 se traduce a validacion por ser un error 4xx no reconocido`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode(418, "I'm a teapot"),
            """{"status":418,"message":"No soy una tetera"}"""
        )

        assertEquals(ErrorApi.Validacion("No soy una tetera"), error)
    }

    @Test
    fun `un cuerpo que no es JSON usa el mensaje por defecto del codigo`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderPlano("Error sin formato", HttpStatusCode.BadRequest)
            }
        )

        try {
            val error = capturarError { cliente.get("productos") }

            assertEquals(
                ErrorApi.Validacion("El servidor rechazó los datos enviados."),
                error
            )
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un JSON sin message usa el mensaje por defecto`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.NotFound,
            """{"status":404}"""
        )

        assertEquals(
            ErrorApi.NoEncontrado("No se encontró el recurso solicitado."),
            error
        )
    }

    @Test
    fun `un mensaje en blanco usa el mensaje por defecto`() = runTest {
        val error = errorDeRespuesta(
            HttpStatusCode.Conflict,
            """{"status":409,"message":"   "}"""
        )

        assertEquals(
            ErrorApi.Conflicto("La operación entra en conflicto con el estado actual."),
            error
        )
    }

    @Test
    fun `una IOException se traduce a sin conexion`() = runTest {
        val error = assertIs<ErrorApi.SinConexion>(
            IOException("sin red").aErrorApi()
        )

        assertEquals(
            "No se pudo conectar con el servidor. Verifica tu conexión.",
            error.mensaje
        )
    }

    @Test
    fun `un timeout de solicitud se traduce a tiempo agotado`() = runTest {
        val error = assertIs<ErrorApi.TiempoAgotado>(
            HttpRequestTimeoutException("http://localhost", 1_000).aErrorApi()
        )

        assertEquals(
            "La solicitud tardó demasiado tiempo. Inténtalo de nuevo.",
            error.mensaje
        )
    }

    @Test
    fun `un timeout de conexion se traduce a sin conexion`() = runTest {
        val error = assertIs<ErrorApi.SinConexion>(
            ConnectTimeoutException("sin conexión").aErrorApi()
        )

        assertEquals(
            "No se pudo conectar con el servidor. Verifica tu conexión.",
            error.mensaje
        )
    }

    @Test
    fun `un timeout de socket se traduce a tiempo agotado`() = runTest {
        val error = assertIs<ErrorApi.TiempoAgotado>(
            SocketTimeoutException("socket lento").aErrorApi()
        )

        assertEquals(
            "El servidor tardó demasiado en responder. Inténtalo de nuevo.",
            error.mensaje
        )
    }

    @Test
    fun `una excepcion inesperada se traduce a error de servidor`() = runTest {
        val error = assertIs<ErrorApi.Servidor>(
            IllegalStateException("inesperado").aErrorApi()
        )

        assertEquals("inesperado", error.mensaje)
    }

    @Test
    fun `una cancelacion no se convierte en error de API`() = runTest {
        assertFailsWith<CancellationException> {
            alAplicacion<Unit> {
                throw CancellationException("cancelado")
            }
        }
    }

    private suspend fun errorDeRespuesta(
        estado: HttpStatusCode,
        cuerpo: String
    ): ErrorApi {
        val cliente = crearHttpClient(
            motor = MockEngine { responderJson(cuerpo, estado) }
        )

        return try {
            capturarError { cliente.get("productos") }
        } finally {
            cliente.close()
        }
    }

    private suspend fun capturarError(bloque: suspend () -> Unit): ErrorApi {
        val fallo = try {
            alAplicacion { bloque() }
            null
        } catch (error: ErrorDeApi) {
            error
        }

        return assertNotNull(fallo).error
    }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.responderPlano(
        cuerpo: String,
        estado: HttpStatusCode
    ) = respond(
        content = cuerpo,
        status = estado,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString())
    )
}
