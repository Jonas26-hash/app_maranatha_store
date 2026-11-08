package com.maranathastore.app.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RolTest {

    @Test
    fun `ADMIN del backend se mapea a administrador`() {
        assertEquals(Rol.ADMIN, Rol.desdeApi("ADMIN"))
    }

    @Test
    fun `USER del backend se mapea a usuario`() {
        assertEquals(Rol.USER, Rol.desdeApi("USER"))
    }

    @Test
    fun `el valor se compara sin distinguir mayusculas`() {
        assertEquals(Rol.ADMIN, Rol.desdeApi("admin"))
    }

    @Test
    fun `un rol desconocido no bloquea el acceso y cae a usuario`() {
        assertEquals(Rol.USER, Rol.desdeApi("SUPERVISOR"))
    }

    @Test
    fun `un rol vacio cae a usuario`() {
        assertEquals(Rol.USER, Rol.desdeApi(""))
    }

    @Test
    fun `las etiquetas son las esperadas por la interfaz`() {
        assertEquals("Administrador", Rol.ADMIN.etiqueta)
        assertEquals("Usuario", Rol.USER.etiqueta)
        assertTrue(Rol.entries.isNotEmpty())
    }
}
