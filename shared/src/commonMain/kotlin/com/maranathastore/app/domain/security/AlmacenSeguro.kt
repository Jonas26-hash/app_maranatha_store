package com.maranathastore.app.domain.security

import com.maranathastore.app.domain.auth.Sesion

/**
 * Guarda la sesión cifrada en el almacén seguro de cada plataforma:
 * Keystore + AES/GCM en Android y Keychain en iOS.
 *
 * El token JWT nunca se guarda en texto plano ni se escribe en logs.
 */
interface AlmacenSeguro {

    fun guardarSesion(sesion: Sesion)

    fun leerSesion(): Sesion?

    fun borrarSesion()
}
