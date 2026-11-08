package com.maranathastore.app.data.security

import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SesionSeguraTest {

    @Test
    fun `una sesion se serializa y recupera sin perdida`() {
        val original = Sesion("jwt-largo.con.puntos", "admin", Rol.ADMIN)

        val recuperada = original.aJsonSeguro().aSesionSegura()

        assertEquals(original, recuperada)
    }

    @Test
    fun `el rol de usuario tambien se conserva`() {
        val original = Sesion("jwt", "operador", Rol.USER)

        assertEquals(original, original.aJsonSeguro().aSesionSegura())
    }

    @Test
    fun `un JSON invalido no produce sesion`() {
        assertNull("esto no es JSON".aSesionSegura())
    }

    @Test
    fun `un JSON incompleto no produce sesion`() {
        assertNull("""{"token":"jwt"}""".aSesionSegura())
    }

    @Test
    fun `un token vacio no produce sesion`() {
        assertNull(
            """{"token":"","usuario":"admin","rol":"ADMIN"}""".aSesionSegura()
        )
    }

    @Test
    fun `un rol desconocido cae a usuario regular`() {
        val sesion = """{"token":"jwt","usuario":"admin","rol":"ROOT"}""".aSesionSegura()

        assertEquals(Rol.USER, sesion?.rol)
    }

    @Test
    fun `un rol en minusculas no reconocido cae a usuario regular`() {
        val sesion = """{"token":"jwt","usuario":"admin","rol":"admin"}""".aSesionSegura()

        // El formato persistido usa Rol.name; un valor ajeno se degrada.
        assertEquals(Rol.USER, sesion?.rol)
    }
}
