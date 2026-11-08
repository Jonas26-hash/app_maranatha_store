package com.maranathastore.app.domain.producto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrecioTest {

    @Test
    fun `formatea centavos con separador de miles`() {
        val precio = Precio.desdeCentavos(1234567)
        assertEquals("S/ 12,345.67", precio.formateado())
    }

    @Test
    fun `texto plano mantiene dos decimales`() {
        val precio = Precio.desdeCentavos(1250)
        assertEquals("12.50", precio.textoPlano())
    }

    @Test
    fun `desde texto acepta coma y dos decimales`() {
        val precio = Precio.desdeTexto("12,5")
        assertEquals(1250, precio?.centavos)
    }

    @Test
    fun `desde texto rechaza formatos invalidos`() {
        assertNull(Precio.desdeTexto(""))
        assertNull(Precio.desdeTexto("abc"))
        assertNull(Precio.desdeTexto("12.345"))
        assertNull(Precio.desdeTexto("-1"))
        assertNull(Precio.desdeTexto("1.2.3"))
    }

    @Test
    fun `desde texto admite hasta diez digitos enteros`() {
        val precio = Precio.desdeTexto("9999999999.99")
        assertEquals(999999999999, precio?.centavos)
    }

    @Test
    fun `desde double redondea a centavos`() {
        val precio = Precio.desdeDouble(12.345)
        assertEquals(1235, precio.centavos)
    }

    @Test
    fun `es positivo solo cuando los centavos superan cero`() {
        assertTrue(Precio.desdeCentavos(1).esPositivo)
        assertEquals(false, Precio.CERO.esPositivo)
    }
}