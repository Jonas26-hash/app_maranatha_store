package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun crearMotorHttp(): HttpClientEngine

private fun Url.esLogin(): Boolean = encodedPath.endsWith("/auth/login")

/**
 * Cliente HTTP compartido por Android e iOS.
 *
 * - Adjunta el JWT como Bearer en todas las rutas protegidas.
 * - Nunca adjunta el token al login.
 * - Si una ruta protegida responde 401, notifica a la app para cerrar sesión.
 * - El header Authorization se sanea en los logs.
 */
fun crearHttpClient(
    motor: HttpClientEngine,
    leerToken: suspend () -> String? = { null },
    alExpirarSesion: suspend () -> Unit = {}
): HttpClient = HttpClient(motor) {

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
                explicitNulls = false
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
        sanitizeHeader { header ->
            header.equals(HttpHeaders.Authorization, ignoreCase = true)
        }
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }

    install(Auth) {
        bearer {
            loadTokens {
                leerToken()?.let { BearerTokens(it, null) }
            }

            refreshTokens { null }

            sendWithoutRequest { request ->
                !request.url.build().esLogin()
            }
        }
    }

    defaultRequest {
        url("${ConfigApi.BASE_URL.trimEnd('/')}/")
        accept(ContentType.Application.Json)
        contentType(ContentType.Application.Json)
    }

    HttpResponseValidator {
        // Se usa el manejador de excepciones (no validateResponse) porque la
        // validación por defecto de expectSuccess se ejecuta antes y lanza la
        // excepción. Así la app siempre se entera de un 401 protegido.
        handleResponseExceptionWithRequest { causa, solicitud ->
            if (causa is ResponseException &&
                causa.response.status == HttpStatusCode.Unauthorized &&
                !solicitud.url.esLogin()
            ) {
                alExpirarSesion()
            }
        }
    }

    expectSuccess = true
}
