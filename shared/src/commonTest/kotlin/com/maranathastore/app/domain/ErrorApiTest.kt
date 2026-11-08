package com.maranathastore.app.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ErrorApiTest {

    @Test
    fun `los errores de autenticacion se identifican como tales`() {
        assertTrue(ErrorApi.NoAutorizado("x").esFalloDeAutenticacion)
        assertTrue(ErrorApi.CredencialesInvalidas("x").esFalloDeAutenticacion)
        assertTrue(ErrorApi.SesionExpirada("x").esFalloDeAutenticacion)
    }

    @Test
    fun `los errores de red no se confunden con autenticacion`() {
        assertFalse(ErrorApi.SinConexion("x").esFalloDeAutenticacion)
        assertFalse(ErrorApi.TiempoAgotado("x").esFalloDeAutenticacion)
        assertFalse(ErrorApi.Servidor("x").esFalloDeAutenticacion)
        assertFalse(ErrorApi.Validacion("x").esFalloDeAutenticacion)
    }

    @Test
    fun `los mensajes vacios usan textos por defecto`() {
        assertEquals(
            "El servidor rechazó los datos enviados.",
            ErrorApi.Validacion("").mensajeParaUsuario()
        )
        assertEquals(
            "No se encontró el recurso solicitado.",
            ErrorApi.NoEncontrado(" ").mensajeParaUsuario()
        )
        assertEquals(
            "La operación entra en conflicto con el estado actual.",
            ErrorApi.Conflicto("").mensajeParaUsuario()
        )
        assertEquals(
            "No se pudo conectar con el servidor. Verifica tu conexión.",
            ErrorApi.SinConexion("").mensajeParaUsuario()
        )
        assertEquals(
            "La solicitud tardó demasiado tiempo. Inténtalo de nuevo.",
            ErrorApi.TiempoAgotado("").mensajeParaUsuario()
        )
        assertEquals(
            "No tienes permisos para realizar esta operación.",
            ErrorApi.Prohibido("").mensajeParaUsuario()
        )
        assertEquals(
            "Usuario o contraseña incorrectos.",
            ErrorApi.CredencialesInvalidas("").mensajeParaUsuario()
        )
        assertEquals(
            "Tu sesión expiró. Inicia sesión de nuevo.",
            ErrorApi.SesionExpirada("").mensajeParaUsuario()
        )
        assertEquals(
            "Tu sesión ya no es válida. Inicia sesión de nuevo.",
            ErrorApi.NoAutorizado("").mensajeParaUsuario()
        )
    }

    @Test
    fun `un error de servidor sin codigo usa el mensaje generico`() {
        assertEquals(
            "El servidor no pudo completar la operación. Inténtalo más tarde.",
            ErrorApi.Servidor("").mensajeParaUsuario()
        )
    }

    @Test
    fun `un error de servidor con codigo incluye el codigo`() {
        assertEquals(
            "El servidor devolvió un error (503). Inténtalo más tarde.",
            ErrorApi.Servidor("", 503).mensajeParaUsuario()
        )
    }

    @Test
    fun `el mensaje del backend tiene prioridad cuando no esta vacio`() {
        assertEquals(
            "Stock insuficiente para el producto",
            ErrorApi.Validacion("Stock insuficiente para el producto").mensajeParaUsuario()
        )
    }

    @Test
    fun `ErrorDeApi expone el mensaje traducido`() {
        val fallo = ErrorDeApi(ErrorApi.Conflicto(""))

        assertEquals("La operación entra en conflicto con el estado actual.", fallo.mensaje)
        assertEquals(fallo.mensaje, fallo.message)
    }
}
