package com.maranathastore.app.domain.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidacionProductoTest {

    @Test
    fun `codigo vacio es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "   ",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El código del artículo es obligatorio.", resultado.errores.codigo)
    }

    @Test
    fun `codigo demasiado largo es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "A".repeat(31),
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertTrue(resultado.errores.codigo?.contains("30") == true)
    }

    @Test
    fun `codigo con caracteres no permitidos es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART 001*",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El código solo puede contener letras, números, guiones y puntos.", resultado.errores.codigo)
    }

    @Test
    fun `codigo duplicado es rechazado`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-001",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5",
            codigosExistentes = setOf("ART-001", "ART-002")
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El código 'ART-001' ya está registrado en otro artículo.", resultado.errores.codigo)
    }

    @Test
    fun `codigo duplicado detecta diferencias de mayusculas y espacios`() {
        val resultado = ValidacionProducto.validar(
            codigo = "  art-001  ",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5",
            codigosExistentes = setOf("ART-001")
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El código 'ART-001' ya está registrado en otro artículo.", resultado.errores.codigo)
    }

    @Test
    fun `nombre vacio es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "   ",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El nombre del artículo es obligatorio.", resultado.errores.nombre)
    }

    @Test
    fun `nombre demasiado largo es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "a".repeat(151),
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertTrue(resultado.errores.nombre?.contains("150") == true)
    }

    @Test
    fun `descripcion demasiado larga es invalida`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "a".repeat(501),
            precioTexto = "10.00",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertTrue(resultado.errores.descripcion?.contains("500") == true)
    }

    @Test
    fun `precio cero es invalido por regla de la app`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "0",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El precio debe ser mayor que 0.", resultado.errores.precio)
    }

    @Test
    fun `precio negativo es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "-1",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertTrue(resultado.errores.precio != null)
    }

    @Test
    fun `precio vacio es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "",
            stockTexto = "5"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("Ingresa el precio del artículo.", resultado.errores.precio)
    }

    @Test
    fun `stock negativo es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "-1"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("El stock no puede ser negativo.", resultado.errores.stock)
    }

    @Test
    fun `stock no numerico es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = "abc"
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("Ingresa un stock válido (número entero).", resultado.errores.stock)
    }

    @Test
    fun `stock vacio es invalido`() {
        val resultado = ValidacionProducto.validar(
            codigo = "ART-01",
            nombre = "Lapicero",
            descripcion = "",
            precioTexto = "10.00",
            stockTexto = ""
        )
        assertIs<ResultadoValidacionProducto.Invalido>(resultado)
        assertEquals("Ingresa el stock inicial.", resultado.errores.stock)
    }

    @Test
    fun `validacion exitosa normaliza descripcion vacia a null y codigo a mayusculas`() {
        val resultado = ValidacionProducto.validar(
            codigo = "  art-01  ",
            nombre = "  Lapicero  ",
            descripcion = "   ",
            precioTexto = "10.5",
            stockTexto = "5",
            activo = false
        )
        val valido = assertIs<ResultadoValidacionProducto.Valido>(resultado)
        assertEquals("ART-01", valido.datos.codigo)
        assertEquals("Lapicero", valido.datos.nombre)
        assertNull(valido.datos.descripcion)
        assertEquals(1050, valido.datos.precio.centavos)
        assertEquals(5, valido.datos.stock)
        assertEquals(false, valido.datos.activo)
    }

    @Test
    fun `validacion exitosa conserva descripcion`() {
        val resultado = ValidacionProducto.validar(
            codigo = "BIB-01",
            nombre = "Biblia",
            descripcion = "  Reina Valera  ",
            precioTexto = "49.90",
            stockTexto = "12"
        )
        val valido = assertIs<ResultadoValidacionProducto.Valido>(resultado)
        assertEquals("BIB-01", valido.datos.codigo)
        assertEquals("Reina Valera", valido.datos.descripcion)
    }

    @Test
    fun `validar cantidad rechaza cero y texto`() {
        assertIs<ResultadoCantidad.Invalida>(ValidacionProducto.validarCantidad(""))
        assertIs<ResultadoCantidad.Invalida>(ValidacionProducto.validarCantidad("0"))
        assertIs<ResultadoCantidad.Invalida>(ValidacionProducto.validarCantidad("-1"))
        assertIs<ResultadoCantidad.Invalida>(ValidacionProducto.validarCantidad("abc"))
    }

    @Test
    fun `validar cantidad acepta entero positivo`() {
        val resultado = ValidacionProducto.validarCantidad("7")
        val valida = assertIs<ResultadoCantidad.Valida>(resultado)
        assertEquals(7, valida.cantidad)
    }
}
