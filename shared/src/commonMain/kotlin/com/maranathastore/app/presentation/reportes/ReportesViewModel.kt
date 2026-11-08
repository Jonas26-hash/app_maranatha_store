package com.maranathastore.app.presentation.reportes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.domain.venta.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportesUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val ingresosTotales: Double = 0.0,
    val cantidadOperaciones: Int = 0,
    val productosStockBajo: Int = 0,
    val productosAgotados: Int = 0
)

class ReportesViewModel(
    private val ventaRepository: VentaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.value = ReportesUiState(cargando = true)
            try {
                val ventas = ventaRepository.listar()
                val productos = productoRepository.listar()
                val ventasValidas = ventas.filterNot { it.estado.equals("ANULADA", ignoreCase = true) }
                _uiState.value = ReportesUiState(
                    cargando = false,
                    ingresosTotales = ventasValidas.sumOf { it.total ?: 0.0 },
                    cantidadOperaciones = ventasValidas.size,
                    productosStockBajo = productos.count { it.stock <= 5 },
                    productosAgotados = productos.count { it.stock == 0 }
                )
            } catch (e: Exception) {
                _uiState.value = ReportesUiState(
                    cargando = false,
                    error = e.message ?: "No se pudieron cargar los reportes."
                )
            }
        }
    }
}
