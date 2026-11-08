package com.maranathastore.app.data.tema

import android.content.Context

class PreferenciasTemaAndroid(
    context: Context
) : PreferenciasTema {

    private val preferencias = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun leerModoTema(): ModoTema {
        val guardado = preferencias.getString(CLAVE_MODO, null) ?: return ModoTema.OSCURO
        return runCatching { ModoTema.valueOf(guardado) }.getOrDefault(ModoTema.OSCURO)
    }

    override fun guardarModoTema(modo: ModoTema) {
        preferencias.edit().putString(CLAVE_MODO, modo.name).apply()
    }

    private companion object {
        const val PREFS = "maranathastore_tema"
        const val CLAVE_MODO = "modo_tema"
    }
}
