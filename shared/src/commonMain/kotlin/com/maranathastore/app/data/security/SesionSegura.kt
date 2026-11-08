package com.maranathastore.app.data.security

import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SesionGuardada(
    val token: String,
    val usuario: String,
    val rol: String
)

private val jsonSesion = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

internal fun Sesion.aJsonSeguro(): String = jsonSesion.encodeToString(
    SesionGuardada(
        token = token,
        usuario = usuario,
        rol = rol.name
    )
)

internal fun String.aSesionSegura(): Sesion? = try {
    val guardada = jsonSesion.decodeFromString<SesionGuardada>(this)
    if (guardada.token.isBlank()) {
        null
    } else {
        Sesion(
            token = guardada.token,
            usuario = guardada.usuario,
            rol = runCatching { Rol.valueOf(guardada.rol) }.getOrDefault(Rol.USER)
        )
    }
} catch (_: Exception) {
    null
}
