package com.maranathastore.app.domain.producto

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Dinero representado en centavos para evitar los errores de punto flotante
 * de Double en el cliente. El backend sigue siendo la única fuente de verdad
 * para totales; este tipo solo se usa para mostrar y enviar valores.
 */
@JvmInline
value class Precio private constructor(val centavos: Long) {

    val esPositivo: Boolean
        get() = centavos > 0

    val comoDouble: Double
        get() = centavos / 100.0

    fun formateado(moneda: String = "S/"): String {
        val signo = if (centavos < 0) "-" else ""
        val absoluto = abs(centavos)
        val enteros = absoluto / 100
        val decimales = absoluto % 100
        return "$moneda $signo${agruparEnteros(enteros)}.${decimales.toString().padStart(2, '0')}"
    }

    fun textoPlano(): String {
        val absoluto = abs(centavos)
        val signo = if (centavos < 0) "-" else ""
        return "$signo${absoluto / 100}.${(absoluto % 100).toString().padStart(2, '0')}"
    }

    companion object {
        val CERO = Precio(0)

        fun desdeCentavos(valor: Long): Precio = Precio(valor)

        fun desdeDouble(valor: Double): Precio =
            Precio((valor * 100).roundToLong())

        fun desdeTexto(texto: String): Precio? {
            val normalizado = texto.trim().replace(',', '.')
            if (!Regex("""^\d{1,10}(\.\d{1,2})?$""").matches(normalizado)) return null

            val partes = normalizado.split('.')
            val entero = partes[0].toLongOrNull() ?: return null
            val decimales = partes.getOrNull(1)
                ?.padEnd(2, '0')
                ?.toLongOrNull() ?: 0

            return Precio(entero * 100 + decimales)
        }
    }
}

private fun agruparEnteros(valor: Long): String {
    val texto = valor.toString()
    if (texto.length <= 3) return texto
    return texto.reversed().chunked(3).joinToString(",").reversed()
}