package com.maranathastore.app.util

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual fun fechaHoyIso(): String {
    val formato = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "en_US_POSIX")
        dateFormat = "yyyy-MM-dd"
    }
    return formato.stringFromDate(NSDate())
}

actual fun fechaLargaEspanol(): String {
    val formato = NSDateFormatter().apply {
        locale = NSLocale(localeIdentifier = "es_ES")
        dateFormat = "EEEE d 'de' MMMM"
    }
    return formato.stringFromDate(NSDate()).replaceFirstChar { caracter ->
        if (caracter.isLowerCase()) caracter.uppercaseChar().toString() else caracter.toString()
    }
}

actual fun horaDelDia(): Int {
    val formato = NSDateFormatter().apply { dateFormat = "H" }
    return formato.stringFromDate(NSDate()).toIntOrNull() ?: 12
}
