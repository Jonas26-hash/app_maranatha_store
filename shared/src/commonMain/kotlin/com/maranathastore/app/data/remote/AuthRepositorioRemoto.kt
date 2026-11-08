package com.maranathastore.app.data.remote

import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.mensajeParaUsuario
import com.maranathastore.app.domain.auth.AuthRepository
import com.maranathastore.app.domain.auth.Credenciales
import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.clearAuthTokens

class AuthRepositorioRemoto(
    private val api: AuthApi,
    private val cliente: HttpClient
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val respuesta = try {
            alAplicacion {
                api.iniciarSesion(
                    LoginRequestDto(
                        username = credenciales.usuario,
                        password = credenciales.contrasena
                    )
                )
            }
        } catch (fallo: ErrorDeApi) {
            val error = fallo.error
            throw if (error is ErrorApi.NoAutorizado) {
                ErrorDeApi(
                    ErrorApi.CredencialesInvalidas(error.mensajeParaUsuario())
                )
            } else {
                fallo
            }
        }

        val sesion = respuesta.aDominio()

        // El plugin Auth conserva el token en memoria; se limpia para que la
        // siguiente petición cargue la sesión recién creada.
        cliente.clearAuthTokens()
        return sesion
    }
}

private fun LoginResponseDto.aDominio(): Sesion {
    if (token.isBlank()) {
        throw ErrorDeApi(
            ErrorApi.Servidor("El servidor devolvió una sesión sin token.")
        )
    }

    return Sesion(
        token = token,
        usuario = username.ifBlank { "usuario" },
        rol = Rol.desdeApi(rol)
    )
}
