package com.maranathastore.app.util

/** Fecha de hoy en formato ISO corto: `yyyy-MM-dd`. */
expect fun fechaHoyIso(): String

/** Fecha larga en español, por ejemplo `Viernes 6 de noviembre`. */
expect fun fechaLargaEspanol(): String

/** Hora actual del dispositivo en formato 0..23. */
expect fun horaDelDia(): Int
