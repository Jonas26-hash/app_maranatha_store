package com.maranathastore.app.presentation.clientes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.ClienteApi
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.data.remote.ClienteRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ClientesUiState {
    data object Loading : ClientesUiState
    data class Success(val clientes: List<ClienteDto>) : ClientesUiState
    data object Empty : ClientesUiState
    data class Error(val mensaje: String) : ClientesUiState
}

class ClientesViewModel(private val api: ClienteApi) : ViewModel() {
    private val _state = MutableStateFlow<ClientesUiState>(ClientesUiState.Loading)
    val state: StateFlow<ClientesUiState> = _state.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _state.value = ClientesUiState.Loading
            try {
                val lista = api.listar()
                _state.value = if (lista.isEmpty()) ClientesUiState.Empty else ClientesUiState.Success(lista)
            } catch (e: Exception) {
                _state.value = ClientesUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun crear(dto: ClienteRequestDto) {
        viewModelScope.launch {
            try {
                api.crear(dto)
                cargar()
            } catch (e: Exception) {
                _state.value = ClientesUiState.Error(e.message ?: "Error")
            }
        }
    }
}