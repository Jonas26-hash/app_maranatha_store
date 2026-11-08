package com.maranathastore.app.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.MaranathaAiApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AutorMensaje { USUARIO, ASISTENTE }

data class MensajeChat(
    val autor: AutorMensaje,
    val texto: String
)

data class MaranathaAiUiState(
    val entrada: String = "",
    val enviando: Boolean = false,
    val mensajes: List<MensajeChat> = emptyList(),
    val error: String? = null
)

class MaranathaAIViewModel(
    private val api: MaranathaAiApi
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaranathaAiUiState())
    val uiState: StateFlow<MaranathaAiUiState> = _uiState.asStateFlow()

    fun onEntradaChange(valor: String) {
        _uiState.update { it.copy(entrada = valor, error = null) }
    }

    fun enviar() {
        val texto = _uiState.value.entrada.trim()
        if (texto.isBlank() || _uiState.value.enviando) return
        _uiState.update {
            it.copy(
                entrada = "",
                enviando = true,
                error = null,
                mensajes = it.mensajes + MensajeChat(AutorMensaje.USUARIO, texto)
            )
        }
        viewModelScope.launch {
            try {
                val respuesta = api.enviarMensaje(texto)
                val reply = respuesta.reply?.takeIf { it.isNotBlank() } ?: "Sin respuesta del asistente."
                _uiState.update {
                    it.copy(
                        enviando = false,
                        mensajes = it.mensajes + MensajeChat(AutorMensaje.ASISTENTE, reply)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        enviando = false,
                        error = e.message ?: "No se pudo contactar a Maranatha AI."
                    )
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}
