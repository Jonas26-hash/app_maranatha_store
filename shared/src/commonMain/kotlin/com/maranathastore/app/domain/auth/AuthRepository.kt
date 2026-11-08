package com.maranathastore.app.domain.auth

interface AuthRepository {

    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
}
