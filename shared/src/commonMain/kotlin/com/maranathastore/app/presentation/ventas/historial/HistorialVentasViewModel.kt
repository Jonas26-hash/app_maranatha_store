package com.maranathastore.app.presentation.ventas.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.VentaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistorialVentasViewModel(private val api: VentaApi) : ViewModel() {
    private val _ventas = MutableStateFlow<List<VentaDto>>(emptyList())
    val ventas: StateFlow<List<VentaDto>> = _ventas.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch { _ventas.value = api.listar() }
    }

    fun anular(id: Long) {
        viewModelScope.launch {
            api.anular(id)
            cargar()
        }
    }
}