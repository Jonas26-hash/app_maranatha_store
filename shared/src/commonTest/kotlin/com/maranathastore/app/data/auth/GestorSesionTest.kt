package com.maranathastore.app.data.auth

import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.support.AlmacenSeguroFalso
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GestorSesionTest {

    private val admin = Sesion("jwt-admin", "admin", Rol.ADMIN)
    private val usuario = Sesion("jwt-user", "operador", Rol.USER)

    @Test
    fun `restaura la sesion existente al construirse`() {
        val gestor = GestorSesion(AlmacenSeguroFalso(admin))

        assertEquals(admin, gestor.sesion.value)
        assertEquals("jwt-admin", gestor.token())
    }

    @Test
    fun `sin sesion guardada el estado inicia vacio`() {
        val gestor = GestorSesion(AlmacenSeguroFalso())

        assertNull(gestor.sesion.value)
        assertNull(gestor.token())
    }

    @Test
    fun `establecer persiste la sesion y actualiza el flujo`() {
        val almacen = AlmacenSeguroFalso()
        val gestor = GestorSesion(almacen)

        gestor.establecer(usuario)

        assertEquals(usuario, almacen.sesionGuardada)
        assertEquals(1, almacen.guardados)
        assertEquals(usuario, gestor.sesion.value)
        assertEquals("jwt-user", gestor.token())
    }

    @Test
    fun `limpiar borra el almacenamiento y deja el flujo vacio`() {
        val almacen = AlmacenSeguroFalso(admin)
        val gestor = GestorSesion(almacen)

        gestor.limpiar()

        assertEquals(1, almacen.borrados)
        assertNull(almacen.sesionInicial)
        assertNull(gestor.sesion.value)
        assertNull(gestor.token())
    }

    @Test
    fun `cambiar de sesion reemplaza el valor anterior`() {
        val almacen = AlmacenSeguroFalso(admin)
        val gestor = GestorSesion(almacen)

        gestor.establecer(usuario)

        assertEquals(usuario, gestor.sesion.value)
        assertEquals(usuario, almacen.sesionInicial)
    }
}
