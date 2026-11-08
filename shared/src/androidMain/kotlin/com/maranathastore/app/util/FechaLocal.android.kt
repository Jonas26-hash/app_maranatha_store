package com.maranathastore.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val localeEspanol = Locale.forLanguageTag("es-ES")

actual fun fechaHoyIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

actual fun fechaLargaEspanol(): String {
    val texto = SimpleDateFormat("EEEE d 'de' MMMM", localeEspanol).format(Date())
    return texto.replaceFirstChar { caracter ->
        if (caracter.isLowerCase()) caracter.uppercaseChar().toString() else caracter.toString()
    }
}

actual fun horaDelDia(): Int =
    Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
