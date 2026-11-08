package com.maranathastore.app.data.remote

import com.maranathastore.app.support.autorizacion
import com.maranathastore.app.support.responderJson
import com.maranathastore.app.support.ruta
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HttpClientFactoryTest {

    @Test
    fun `el login no adjunta el token aunque exista una sesion`() = runTest {
        val solicitudes = mutableListOf<String?>()

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                solicitudes += solicitud.autorizacion()
                responderJson(
                    """{"token":"nuevo","username":"admin","rol":"ADMIN"}"""
                )
            },
            leerToken = { "token-anterior" }
        )

        try {
            AuthApi(cliente).iniciarSesion(
                LoginRequestDto(username = "admin", password = "secreto")
            )

            assertEquals(1, solicitudes.size)
            assertNull(solicitudes.single())
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `las rutas protegidas adjuntan el token como Bearer`() = runTest {
        val autorizaciones = mutableListOf<String?>()

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                autorizaciones += solicitud.autorizacion()
                responderJson("{}")
            },
            leerToken = { "jwt-de-prueba" }
        )

        try {
            cliente.get("productos")

            assertEquals("Bearer jwt-de-prueba", autorizaciones.single())
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 401 en ruta protegida notifica la expiracion de la sesion`() = runTest {
        var expiraciones = 0

        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"status":401,"message":"No autorizado"}""",
                    HttpStatusCode.Unauthorized
                )
            },
            leerToken = { "jwt-vencido" },
            alExpirarSesion = { expiraciones++ }
        )

        try {
            assertFails {
                cliente.get("productos")
            }

            assertEquals(1, expiraciones)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 401 en el login no notifica expiracion de sesion`() = runTest {
        var expiraciones = 0

        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"status":401,"message":"Credenciales incorrectas"}""",
                    HttpStatusCode.Unauthorized
                )
            },
            leerToken = { "jwt-vencido" },
            alExpirarSesion = { expiraciones++ }
        )

        try {
            assertFails {
                cliente.post("auth/login")
            }

            assertEquals(0, expiraciones)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `el login se envia al endpoint real del gateway`() = runTest {
        val rutas = mutableListOf<String>()
        val metodos = mutableListOf<String>()

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                rutas += solicitud.ruta()
                metodos += solicitud.method.value
                responderJson(
                    """{"token":"jwt","username":"admin","rol":"ADMIN"}"""
                )
            }
        )

        try {
            AuthApi(cliente).iniciarSesion(
                LoginRequestDto(username = "admin", password = "secreto")
            )

            assertTrue(rutas.single().endsWith("/auth/login"))
            assertEquals("POST", metodos.single())
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `el contenido JSON se envia con Content-Type correcto`() = runTest {
        var contentTypes = emptyList<String>()

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                contentTypes = listOfNotNull(solicitud.body.contentType?.toString())
                responderJson(
                    """{"token":"jwt","username":"admin","rol":"ADMIN"}"""
                )
            }
        )

        try {
            AuthApi(cliente).iniciarSesion(
                LoginRequestDto(username = "admin", password = "secreto")
            )

            assertTrue(contentTypes.any { it.startsWith("application/json") })
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 200 con cuerpo vacio no falla para peticiones sin deserializacion`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine { responderJson("") }
        )

        try {
            // No se debe confundir un cuerpo vacío con un 401 de sesión.
            cliente.get("productos")
        } finally {
            cliente.close()
        }
    }
}
