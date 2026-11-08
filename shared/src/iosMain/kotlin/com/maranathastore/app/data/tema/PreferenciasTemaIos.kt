package com.maranathastore.app.data.tema

import platform.Foundation.NSUserDefaults

class PreferenciasTemaIos : PreferenciasTema {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun leerModoTema(): ModoTema {
        val guardado = defaults.stringForKey(CLAVE_MODO) ?: return ModoTema.OSCURO
        return runCatching { ModoTema.valueOf(guardado) }.getOrDefault(ModoTema.OSCURO)
    }

    override fun guardarModoTema(modo: ModoTema) {
        defaults.setObject(modo.name, forKey = CLAVE_MODO)
    }

    private companion object {
        const val CLAVE_MODO = "modo_tema"
    }
}
