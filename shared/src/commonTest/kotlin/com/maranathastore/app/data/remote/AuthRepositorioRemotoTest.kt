package com.maranathastore.app.data.remote

import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.auth.Credenciales
import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.support.AlmacenSeguroFalso
import com.maranathastore.app.support.autorizacion
import com.maranathastore.app.support.responderJson
import com.maranathastore.app.support.ruta
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class AuthRepositorioRemotoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `mapea la respuesta real del login y envia el contrato correcto`() = runTest {
        var cuerpo: LoginRequestDto? = null
        var autorizacion: String? = null

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                autorizacion = solicitud.autorizacion()
                cuerpo = json.decodeFromString<LoginRequestDto>(
                    solicitud.body.toByteArray().decodeToString()
                )
                responderJson(
                    """{"token":"jwt-real","username":"admin","rol":"ADMIN"}"""
                )
            }
        )

        try {
            val repositorio = AuthRepositorioRemoto(AuthApi(cliente), cliente)

            val sesion = repositorio.iniciarSesion(
                Credenciales(usuario = "admin", contrasena = "secreto")
            )

            assertEquals(Sesion("jwt-real", "admin", Rol.ADMIN), sesion)
            assertEquals(LoginRequestDto("admin", "secreto"), cuerpo)
            assertNull(autorizacion)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un rol desconocido se trata como usuario regular`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"token":"jwt","username":"operador","rol":"SUPERVISOR"}"""
                )
            }
        )

        try {
            val sesion = AuthRepositorioRemoto(AuthApi(cliente), cliente)
                .iniciarSesion(Credenciales("operador", "secreto"))

            assertEquals(Rol.USER, sesion.rol)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un username vacio no produce una sesion sin usuario visible`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson("""{"token":"jwt","username":"","rol":"USER"}""")
            }
        )

        try {
            val sesion = AuthRepositorioRemoto(AuthApi(cliente), cliente)
                .iniciarSesion(Credenciales("operador", "secreto"))

            assertEquals("usuario", sesion.usuario)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un token vacio se rechaza como error del servidor`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson("""{"token":"","username":"admin","rol":"ADMIN"}""")
            }
        )

        try {
            val fallo = assertFailsWith<ErrorDeApi> {
                AuthRepositorioRemoto(AuthApi(cliente), cliente)
                    .iniciarSesion(Credenciales("admin", "secreto"))
            }

            assertIs<ErrorApi.Servidor>(fallo.error)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 401 del login se traduce a credenciales invalidas con el mensaje del backend`() = runTest {
        var expiraciones = 0

        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"status":401,"message":"Credenciales incorrectas"}""",
                    HttpStatusCode.Unauthorized
                )
            },
            leerToken = { "token-viejo" },
            alExpirarSesion = { expiraciones++ }
        )

        try {
            val fallo = assertFailsWith<ErrorDeApi> {
                AuthRepositorioRemoto(AuthApi(cliente), cliente)
                    .iniciarSesion(Credenciales("admin", "mal"))
            }

            val error = assertIs<ErrorApi.CredencialesInvalidas>(fallo.error)
            assertEquals("Credenciales incorrectas", error.mensaje)
            assertEquals(0, expiraciones)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 500 del login conserva el error de servidor`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"status":500,"message":"Fallo interno"}""",
                    HttpStatusCode.InternalServerError
                )
            }
        )

        try {
            val fallo = assertFailsWith<ErrorDeApi> {
                AuthRepositorioRemoto(AuthApi(cliente), cliente)
                    .iniciarSesion(Credenciales("admin", "secreto"))
            }

            val error = assertIs<ErrorApi.Servidor>(fallo.error)
            assertEquals(500, error.codigo)
            assertEquals("Fallo interno", error.mensaje)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `el login exitoso descarta el token cacheado y la siguiente ruta usa el nuevo`() = runTest {
        val autorizaciones = mutableListOf<String?>()
        val gestor = GestorSesion(
            AlmacenSeguroFalso(Sesion("token-viejo", "admin", Rol.ADMIN))
        )

        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                autorizaciones += solicitud.autorizacion()

                if (solicitud.ruta().endsWith("/auth/login")) {
                    responderJson(
                        """{"token":"token-nuevo","username":"admin","rol":"ADMIN"}"""
                    )
                } else {
                    responderJson("{}")
                }
            },
            leerToken = { gestor.token() }
        )

        try {
            cliente.get("productos")
            val sesion = AuthRepositorioRemoto(AuthApi(cliente), cliente)
                .iniciarSesion(Credenciales("admin", "secreto"))

            // En producción el ViewModel persiste la sesión después del login.
            gestor.establecer(sesion)
            cliente.get("productos")

            assertEquals(
                listOf("Bearer token-viejo", null, "Bearer token-nuevo"),
                autorizaciones
            )
        } finally {
            cliente.close()
        }
    }
}
