package com.maranathastore.app.presentation.dashboard

import com.maranathastore.app.domain.venta.Venta

/** Punto de la serie de ventas agrupada por fecha real de la API. */
data class PuntoVentaDiaria(
    val fecha: String,
    val etiqueta: String,
    val total: Double
)

/** Producto que requiere atención por su nivel de stock. */
data class ProductoAlerta(
    val id: Long,
    val nombre: String,
    val stock: Int
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val ventasHoy: Double,
        val ventasMes: Double,
        val totalProductos: Int,
        val stockBajo: Int,
        val stockAgotado: Int,
        val ultimasVentas: List<Venta>,
        val serieVentas: List<PuntoVentaDiaria>,
        val productosBajoStock: List<ProductoAlerta>,
        val productosAgotados: List<ProductoAlerta>,
        val ticketPromedio: Double,
        val ventasValidas: Int
    ) : DashboardUiState
    data object Empty : DashboardUiState
    data class Error(val mensaje: String) : DashboardUiState
}
