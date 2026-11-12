package com.maranathastore.app.presentation.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.ClienteApi
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.VentaRequestDto
import com.maranathastore.app.data.remote.DetalleVentaRequestDto
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.domain.producto.ProductoRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

sealed interface AvisoVentaUi {
    data class Exito(val venta: VentaDto, val mensaje: String) : AvisoVentaUi
    data class Error(val mensaje: String) : AvisoVentaUi
}

data class NuevaVentaUiState(
    val productos: List<Producto> = emptyList(),
    val clientes: List<ClienteDto> = emptyList(),
    val carrito: List<Pair<Producto, Int>> = emptyList(),
    val tipoComprobante: String = "BOLETA",
    val clienteId: Long? = null,
    val enviando: Boolean = false,
    val aviso: AvisoVentaUi? = null
)

class NuevaVentaViewModel(
    private val productoRepository: ProductoRepository,
    private val clienteApi: ClienteApi,
    private val ventaApi: VentaApi
) : ViewModel() {
    private val _state = MutableStateFlow(NuevaVentaUiState())
    val state: StateFlow<NuevaVentaUiState> = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    // Compatibilidad temporal con accesos directos
    val productos = MutableStateFlow<List<Producto>>(emptyList())
    val clientes = MutableStateFlow<List<ClienteDto>>(emptyList())
    val carrito = MutableStateFlow<List<Pair<Producto, Int>>>(emptyList())
    val tipoComprobante = MutableStateFlow("BOLETA")
    val clienteId = MutableStateFlow<Long?>(null)

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            try {
                val prods = productoRepository.listar()
                val cls = try { clienteApi.listar() } catch (_: Exception) { emptyList() }
                _state.update { it.copy(productos = prods, clientes = cls) }
                sincronizarCompat()
            } catch (_: Exception) {
                // Mantener estado actual; la UI puede reintentar desde acciones del usuario si aplica
            }
        }
    }

    fun agregar(producto: Producto, cantidad: Int) {
        if (cantidad <= 0) return
        _state.update { actual ->
            val lista = actual.carrito.toMutableList()
            val idx = lista.indexOfFirst { it.first.id == producto.id }
            if (idx >= 0) {
                val existente = lista[idx]
                lista[idx] = existente.first to (existente.second + cantidad)
            } else {
                lista.add(producto to cantidad)
            }
            actual.copy(carrito = lista, aviso = null)
        }
        sincronizarCompat()
    }

    fun quitarDelCarrito(producto: Producto) {
        _state.update { actual ->
            actual.copy(
                carrito = actual.carrito.filterNot { it.first.id == producto.id },
                aviso = null
            )
        }
        sincronizarCompat()
    }

    fun setTipoComprobante(valor: String) {
        _state.update { it.copy(tipoComprobante = valor) }
        tipoComprobante.value = valor
    }

    fun setClienteId(valor: Long?) {
        _state.update { it.copy(clienteId = valor) }
        clienteId.value = valor
    }

    fun limpiarAviso() {
        _state.update { it.copy(aviso = null) }
    }

    fun enviar() {
        val actual = _state.value
        if (actual.enviando || actual.carrito.isEmpty()) return
        _state.update { it.copy(enviando = true, aviso = null) }
        viewModelScope.launch {
            try {
                val detalles = actual.carrito.map { DetalleVentaRequestDto(it.first.id, it.second) }
                val venta = ventaApi.crear(
                    VentaRequestDto(
                        clienteId = actual.clienteId,
                        tipoComprobante = actual.tipoComprobante,
                        detalles = detalles
                    )
                )
                val comprobante = venta.tipoComprobante ?: actual.tipoComprobante
                val numero = venta.id?.let { "#$it" } ?: ""
                _state.update {
                    it.copy(
                        enviando = false,
                        carrito = emptyList(),
                        aviso = AvisoVentaUi.Exito(
                            venta = venta,
                            mensaje = "Venta emitida correctamente: $comprobante $numero".trim()
                        )
                    )
                }
                cargarDatos()
                sincronizarCompat()
            } catch (e: ResponseException) {
                val mensaje = mensajeErrorVenta(e)
                _state.update { it.copy(enviando = false, aviso = AvisoVentaUi.Error(mensaje)) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        enviando = false,
                        aviso = AvisoVentaUi.Error(e.message ?: "No se pudo emitir la venta.")
                    )
                }
            }
        }
    }

    private suspend fun mensajeErrorVenta(e: ResponseException): String {
        if (e.response.status.value == 409) {
            val cuerpo = try { e.response.bodyAsText() } catch (_: Exception) { "" }
            val msg = try {
                json.parseToJsonElement(cuerpo).jsonObject["message"]?.jsonPrimitive?.contentOrNull
            } catch (_: Exception) { null }
            return msg ?: "Stock insuficiente para completar la venta."
        }
        return try {
            val cuerpo = e.response.bodyAsText()
            json.parseToJsonElement(cuerpo).jsonObject["message"]?.jsonPrimitive?.contentOrNull
                ?: "Error al emitir la venta (${e.response.status.value})."
        } catch (_: Exception) {
            "Error al emitir la venta (${e.response.status.value})."
        }
    }

    private fun sincronizarCompat() {
        val s = _state.value
        productos.value = s.productos
        clientes.value = s.clientes
        carrito.value = s.carrito
        tipoComprobante.value = s.tipoComprobante
        clienteId.value = s.clienteId
    }
}