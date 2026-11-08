package com.maranathastore.app.data.auth

import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.domain.security.AlmacenSeguro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GestorSesion(
    private val almacen: AlmacenSeguro
) {

    private val _sesion = MutableStateFlow(almacen.leerSesion())

    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun token(): String? = _sesion.value?.token

    fun establecer(sesion: Sesion) {
        almacen.guardarSesion(sesion)
        _sesion.value = sesion
    }

    fun limpiar() {
        almacen.borrarSesion()
        _sesion.value = null
    }
}
