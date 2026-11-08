package com.maranathastore.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.domain.producto.UMBRAL_STOCK_BAJO
import com.maranathastore.app.domain.venta.Venta
import com.maranathastore.app.domain.venta.VentaRepository
import com.maranathastore.app.util.fechaHoyIso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val productoRepository: ProductoRepository,
    private val ventaRepository: VentaRepository
) : ViewModel() {
    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _state.value = DashboardUiState.Loading
            try {
                val productos = productoRepository.listar()
                val ventas = ventaRepository.listar()

                // Las ventas anuladas no forman parte de los ingresos reales.
                val validas = ventas.filter { !it.estado.equals("ANULADA", ignoreCase = true) }
                val hoy = fechaHoyIso()
                val mes = hoy.take(7)

                val ventasHoy = validas
                    .filter { fechaDeVenta(it) == hoy }
                    .sumOf { it.total ?: 0.0 }
                val ventasMes = validas
                    .filter { fechaDeVenta(it)?.startsWith(mes) == true }
                    .sumOf { it.total ?: 0.0 }

                val productosBajoStock = productos
                    .filter { it.stock in 1..UMBRAL_STOCK_BAJO }
                    .sortedBy { it.stock }
                val productosAgotados = productos
                    .filter { it.stock <= 0 }
                    .sortedBy { it.nombre }

                val serieVentas = validas
                    .mapNotNull { venta ->
                        fechaDeVenta(venta)?.let { fecha -> fecha to (venta.total ?: 0.0) }
                    }
                    .groupBy({ it.first }, { it.second })
                    .map { (fecha, totales) ->
                        PuntoVentaDiaria(
                            fecha = fecha,
                            etiqueta = etiquetaFecha(fecha),
                            total = totales.sum()
                        )
                    }
                    .sortedBy { it.fecha }
                    .takeLast(7)

                val ultimasVentas = validas
                    .sortedByDescending { fechaDeVenta(it) ?: "" }
                    .take(5)

                val totalValido = validas.sumOf { it.total ?: 0.0 }
                val ticketPromedio = if (validas.isEmpty()) 0.0 else totalValido / validas.size

                _state.value = if (productos.isEmpty() && ventas.isEmpty()) {
                    DashboardUiState.Empty
                } else {
                    DashboardUiState.Success(
                        ventasHoy = ventasHoy,
                        ventasMes = ventasMes,
                        totalProductos = productos.size,
                        stockBajo = productosBajoStock.size,
                        stockAgotado = productosAgotados.size,
                        ultimasVentas = ultimasVentas,
                        serieVentas = serieVentas,
                        productosBajoStock = productosBajoStock.map { it.aAlerta() },
                        productosAgotados = productosAgotados.map { it.aAlerta() },
                        ticketPromedio = ticketPromedio,
                        ventasValidas = validas.size
                    )
                }
            } catch (e: Exception) {
                _state.value = DashboardUiState.Error(e.message ?: "No se pudo cargar el resumen")
            }
        }
    }
}

private fun fechaDeVenta(venta: Venta): String? =
    venta.fecha
        ?.substringBefore('T')
        ?.takeIf { it.length >= 10 }

private fun etiquetaFecha(fecha: String): String =
    if (fecha.length >= 10) {
        "${fecha.substring(8, 10)}/${fecha.substring(5, 7)}"
    } else {
        fecha
    }

private fun com.maranathastore.app.domain.producto.Producto.aAlerta(): ProductoAlerta =
    ProductoAlerta(id = id, nombre = nombre, stock = stock)
