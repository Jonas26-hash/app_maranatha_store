package com.maranathastore.app.data.tema

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencia de apariencia elegida por el usuario. El modo oscuro es el
 * predeterminado para conservar la identidad Liquid Glass Premium.
 */
enum class ModoTema(val etiqueta: String) {
    OSCURO("Oscuro"),
    CLARO("Claro"),
    SISTEMA("Sistema")
}

/** Persistencia de la preferencia de tema en cada plataforma. */
interface PreferenciasTema {
    fun leerModoTema(): ModoTema
    fun guardarModoTema(modo: ModoTema)
}

/**
 * Mantiene el modo de tema activo en memoria y lo persiste al cambiarlo.
 */
class GestorTema(
    private val preferencias: PreferenciasTema
) {
    private val _modo = MutableStateFlow(preferencias.leerModoTema())
    val modo: StateFlow<ModoTema> = _modo.asStateFlow()

    fun cambiar(modo: ModoTema) {
        if (_modo.value == modo) return
        preferencias.guardarModoTema(modo)
        _modo.value = modo
    }
}
