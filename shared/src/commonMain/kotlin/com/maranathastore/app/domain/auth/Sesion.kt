package com.maranathastore.app.domain.auth

enum class Rol {
    ADMIN,
    USER;

    val etiqueta: String
        get() = when (this) {
            ADMIN -> "Administrador"
            USER -> "Usuario"
        }

    companion object {
        /**
         * El backend solo emite ADMIN o USER. Cualquier valor distinto se
         * trata como usuario regular para no bloquear el inicio de sesión si
         * el servidor agregara un rol nuevo.
         */
        fun desdeApi(valor: String): Rol =
            if (valor.equals("ADMIN", ignoreCase = true)) ADMIN else USER
    }
}

data class Sesion(
    val token: String,
    val usuario: String,
    val rol: Rol
)
