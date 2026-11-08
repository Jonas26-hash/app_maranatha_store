package com.maranathastore.app.presentation.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.domain.producto.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventarioViewModel(private val productoRepository: ProductoRepository) : ViewModel() {
    private val _productos = MutableStateFlow<List<com.maranathastore.app.domain.producto.Producto>>(emptyList())
    val productos: StateFlow<List<com.maranathastore.app.domain.producto.Producto>> = _productos.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _productos.value = productoRepository.listar()
        }
    }

    fun stockBajo() = _productos.value.filter { it.stock <= 5 && it.stock > 0 }
    fun agotados() = _productos.value.filter { it.stock == 0 }
}