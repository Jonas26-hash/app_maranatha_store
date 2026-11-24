package com.maranathastore.app.presentation.ventas.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.VentaDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

class HistorialVentasViewModel(private val api: VentaApi) : ViewModel() {
    private val _ventas = MutableStateFlow<List<VentaDto>>(emptyList())
    val ventas: StateFlow<List<VentaDto>> = _ventas.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    suspend fun evidenciaBeta(numero: String) = api.evidenciaBeta(numero)
    suspend fun estadoFiscal(id: Long) = api.estadoFiscal(id)
    suspend fun comprobanteLocal(id: Long) = api.comprobanteLocal(id)

    fun cargar() {
        if (_cargando.value) return
        _cargando.value = true
        viewModelScope.launch {
            try { _error.value = null; _ventas.value = api.listar().sortedByDescending { it.id ?: 0L } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "No se pudo cargar el historial. Pulsa actualizar." }
            finally { _cargando.value = false }
        }
    }

    fun anular(id: Long) {
        viewModelScope.launch {
            try { api.anular(id); cargar() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _error.value = "No se pudo confirmar la anulación. Actualiza el historial." }
        }
    }
}
