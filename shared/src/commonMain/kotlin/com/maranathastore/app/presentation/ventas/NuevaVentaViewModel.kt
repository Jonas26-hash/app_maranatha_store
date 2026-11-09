package com.maranathastore.app.presentation.ventas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.remote.ClienteApi
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.VentaRequestDto
import com.maranathastore.app.data.remote.DetalleVentaRequestDto
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.domain.producto.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NuevaVentaViewModel(
    private val productoRepository: ProductoRepository,
    private val clienteApi: ClienteApi,
    private val ventaApi: VentaApi
) : ViewModel() {
    val productos = MutableStateFlow<List<Producto>>(emptyList())
    val clientes = MutableStateFlow<List<ClienteDto>>(emptyList())
    val carrito = MutableStateFlow<MutableList<Pair<Producto, Int>>>(mutableListOf())
    val tipoComprobante = MutableStateFlow("BOLETA")
    val clienteId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            productos.value = productoRepository.listar()
            clientes.value = clienteApi.listar()
        }
    }

    fun agregar(producto: Producto, cantidad: Int) {
        val list = carrito.value.toMutableList()
        list.add(producto to cantidad)
        carrito.value = list
    }

    fun quitarDelCarrito(producto: Producto) {
        val list = carrito.value.toMutableList()
        list.removeAll { it.first.id == producto.id }
        carrito.value = list
    }

    fun enviar() {
        viewModelScope.launch {
            val detalles = carrito.value.map { DetalleVentaRequestDto(it.first.id, it.second) }
            ventaApi.crear(VentaRequestDto(clienteId.value, tipoComprobante.value, detalles))
            carrito.value = mutableListOf()
        }
    }
}