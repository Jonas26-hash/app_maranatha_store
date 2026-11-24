package com.maranathastore.app.presentation.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.FacturacionApi
import kotlinx.coroutines.CancellationException
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
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
    val documento: String = "",
    val nombre: String = "",
    val direccionFiscal: String = "",
    val consultando: Boolean = false,
    val pagoConfirmado: Boolean = false,
    val requiereRevision: Boolean = false,
    val enviando: Boolean = false,
    val aviso: AvisoVentaUi? = null
)

@OptIn(ExperimentalUuidApi::class)
class NuevaVentaViewModel(
    private val productoRepository: ProductoRepository,
    private val facturacionApi: FacturacionApi,
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
                _state.update { it.copy(productos = prods) }
                sincronizarCompat()
            } catch (_: Exception) {
                // Mantener estado actual; la UI puede reintentar desde acciones del usuario si aplica
            }
        }
    }

    fun agregar(producto: Producto, cantidad: Int) {
        if (cantidad <= 0 || _state.value.enviando || _state.value.requiereRevision) return
        val acumulado = _state.value.carrito.firstOrNull { it.first.id == producto.id }?.second ?: 0
        if (cantidad > producto.stock - acumulado) {
            _state.update { it.copy(aviso = AvisoVentaUi.Error("Stock insuficiente")) }
            return
        }
        _state.update { actual ->
            val lista = actual.carrito.toMutableList()
            val idx = lista.indexOfFirst { it.first.id == producto.id }
            if (idx >= 0) {
                val existente = lista[idx]
                lista[idx] = existente.first to (existente.second + cantidad)
            } else {
                lista.add(producto to cantidad)
            }
            actual.copy(carrito = lista, aviso = null, pagoConfirmado = false)
        }
        sincronizarCompat()
    }

    fun quitarDelCarrito(producto: Producto) {
        if (_state.value.enviando || _state.value.requiereRevision) return
        _state.update { actual ->
            actual.copy(
                carrito = actual.carrito.filterNot { it.first.id == producto.id },
                pagoConfirmado = false,
                aviso = null
            )
        }
        sincronizarCompat()
    }

    fun setTipoComprobante(valor: String) {
        if (_state.value.enviando || _state.value.consultando || _state.value.requiereRevision) return
        _state.update { it.copy(tipoComprobante = valor, documento = "", nombre = "", direccionFiscal = "", pagoConfirmado = false, clienteId = null) }
        sincronizarCompat()
    }

    fun setDocumento(valor: String) {
        if (_state.value.enviando || _state.value.consultando || _state.value.requiereRevision) return
        val limite = if (_state.value.tipoComprobante == "FACTURA") 11 else 8
        _state.update { it.copy(documento = valor.filter { c -> c in '0'..'9' }.take(limite), nombre = "", direccionFiscal = "", pagoConfirmado = false) }
    }

    fun setNombre(valor: String) {
        if (_state.value.enviando || _state.value.consultando || _state.value.requiereRevision) return
        _state.update { it.copy(nombre = valor.take(220), pagoConfirmado = false) }
    }

    fun setDireccionFiscal(valor: String) {
        if (_state.value.enviando || _state.value.consultando || _state.value.requiereRevision) return
        _state.update { it.copy(direccionFiscal = valor.take(250), pagoConfirmado = false) }
    }

    fun confirmarPago(valor: Boolean) {
        if (_state.value.enviando || _state.value.requiereRevision) return
        _state.update { it.copy(pagoConfirmado = valor) }
    }

    fun consultarRuc() {
        val s = _state.value
        if (s.tipoComprobante != "FACTURA" || !s.documento.matches(Regex("[0-9]{11}")) || s.consultando || s.enviando || s.requiereRevision) return
        _state.update { it.copy(consultando = true, nombre = "", direccionFiscal = "", pagoConfirmado = false) }
        viewModelScope.launch {
            try {
                val datos = facturacionApi.consultarRuc(s.documento)
                if (datos.ruc != s.documento || datos.razonSocial.isBlank()) error("Respuesta inválida")
                _state.update { it.copy(nombre = datos.razonSocial.take(220), direccionFiscal = datos.direccionFiscal.orEmpty()) }
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) {
                _state.update { it.copy(aviso = AvisoVentaUi.Error("Consulta no disponible. Ingresa los datos proporcionados por el comprador.")) }
            } finally { _state.update { it.copy(consultando = false) } }
        }
    }

    suspend fun estadoFiscal(id: Long) = ventaApi.estadoFiscal(id)
    suspend fun comprobanteLocal(id: Long) = ventaApi.comprobanteLocal(id)

    fun limpiarAviso() {
        _state.update { it.copy(aviso = null) }
    }

    fun enviar() {
        val actual = _state.value
        if (actual.enviando || actual.consultando || actual.requiereRevision || actual.carrito.isEmpty() || !actual.pagoConfirmado) return
        val identificado = actual.tipoComprobante != "NOTA_PEDIDO"
        val longitud = if (actual.tipoComprobante == "FACTURA") 11 else 8
        if (identificado && (actual.documento.length != longitud || actual.nombre.isBlank())) return
        val clave = Uuid.random().toString()
        _state.update { it.copy(enviando = true, aviso = null) }
        viewModelScope.launch {
            try {
                val detalles = actual.carrito.map { DetalleVentaRequestDto(it.first.id, it.second) }
                val venta = ventaApi.crear(
                    VentaRequestDto(
                        clienteId = null,
                        tipoComprobante = actual.tipoComprobante,
                        detalles = detalles,
                        idempotencyKey = clave,
                        tipoDocumento = if (!identificado) null else if (actual.tipoComprobante == "FACTURA") "RUC" else "DNI",
                        numeroDocumento = actual.documento.takeIf { identificado },
                        clienteNombre = actual.nombre.trim().takeIf { identificado },
                        metodoPago = "EFECTIVO"
                    )
                )
                val comprobante = venta.tipoComprobante ?: actual.tipoComprobante
                val numero = listOfNotNull(venta.serie, venta.numero).joinToString("-").ifBlank { venta.id?.let { "#$it" } ?: "" }
                _state.update {
                    it.copy(
                        enviando = false,
                        carrito = emptyList(),
                        documento = "", nombre = "", direccionFiscal = "", pagoConfirmado = false,
                        aviso = AvisoVentaUi.Exito(
                            venta = venta,
                            mensaje = "Venta emitida correctamente: $comprobante $numero".trim()
                        )
                    )
                }
                cargarDatos()
                sincronizarCompat()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                _state.update { it.copy(enviando = false, requiereRevision = true,
                    aviso = AvisoVentaUi.Error("No se confirmó el registro. Revisa el historial antes de iniciar otra venta. No repitas el cobro.")) }
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
