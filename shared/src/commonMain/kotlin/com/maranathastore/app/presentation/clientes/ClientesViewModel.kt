package com.maranathastore.app.presentation.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.ClienteApi
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.data.remote.ClienteRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientesUiState(
    val cargando: Boolean = true,
    val clientes: List<ClienteDto> = emptyList(),
    val error: String? = null,
    val procesando: Boolean = false,
    val aviso: String? = null
)

class ClientesViewModel(private val api: ClienteApi) : ViewModel() {
    private val _state = MutableStateFlow(ClientesUiState())
    val state: StateFlow<ClientesUiState> = _state.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, error = null) }
            try {
                val lista = api.listar()
                _state.update { it.copy(cargando = false, clientes = lista, error = null) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(cargando = false, error = e.message ?: "No se pudieron cargar los clientes.")
                }
            }
        }
    }

    fun crear(dto: ClienteRequestDto) {
        ejecutarAccion(exito = "Cliente registrado.") { api.crear(dto) }
    }

    fun actualizar(id: Long, dto: ClienteRequestDto) {
        ejecutarAccion(exito = "Cliente actualizado.") { api.actualizar(id, dto) }
    }

    fun eliminar(id: Long) {
        ejecutarAccion(exito = "Cliente eliminado.") { api.eliminar(id) }
    }

    fun limpiarAviso() {
        _state.update { it.copy(aviso = null) }
    }

    private fun ejecutarAccion(exito: String, bloque: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(procesando = true, aviso = null) }
            try {
                bloque()
                _state.update { it.copy(procesando = false, aviso = exito) }
                cargar()
            } catch (e: Exception) {
                _state.update {
                    it.copy(procesando = false, aviso = e.message ?: "No se pudo completar la operación.")
                }
            }
        }
    }
}