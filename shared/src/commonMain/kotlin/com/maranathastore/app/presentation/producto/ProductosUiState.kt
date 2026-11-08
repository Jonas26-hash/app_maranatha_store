package com.maranathastore.app.presentation.producto

import com.maranathastore.app.domain.producto.ErroresProducto

enum class FiltroProductos(val etiqueta: String) {
    TODOS("Todos"),
    STOCK_BAJO("Stock bajo"),
    INACTIVOS("Inactivos")
}

enum class ModoFormulario { CREAR, EDITAR }

data class ProductoFormulario(
    val modo: ModoFormulario,
    val id: Long? = null,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val stock: String = "0",
    val activo: Boolean = true,
    val errores: ErroresProducto = ErroresProducto(),
    val guardando: Boolean = false,
    val errorServidor: String? = null
) {
    val esEdicion: Boolean
        get() = modo == ModoFormulario.EDITAR

    val titulo: String
        get() = if (esEdicion) "Editar artículo" else "Registrar nuevo artículo"

    val puedeGuardar: Boolean
        get() = !guardando
}

enum class OperacionStock { REPONER, DESCONTAR }

data class AjusteStock(
    val productoId: Long,
    val nombre: String,
    val stockActual: Int,
    val activo: Boolean,
    val operacion: OperacionStock = OperacionStock.REPONER,
    val cantidad: String = "",
    val error: String? = null,
    val guardando: Boolean = false
)

data class ConfirmacionEliminar(
    val producto: ProductoUi,
    val eliminando: Boolean = false,
    val error: String? = null
)

sealed interface AvisoUi {
    val mensaje: String

    data class Exito(override val mensaje: String) : AvisoUi
    data class Error(override val mensaje: String) : AvisoUi
}

data class ProductosUiState(
    val cargando: Boolean = true,
    val esAdmin: Boolean = false,
    val productos: List<ProductoUi> = emptyList(),
    val busqueda: String = "",
    val filtro: FiltroProductos = FiltroProductos.TODOS,
    val errorCarga: String? = null,
    val aviso: AvisoUi? = null,
    val formulario: ProductoFormulario? = null,
    val ajusteStock: AjusteStock? = null,
    val confirmacionEliminar: ConfirmacionEliminar? = null
) {
    val visibles: List<ProductoUi>
        get() {
            val q = busqueda.trim().lowercase()
            return productos.filter { producto ->
                val coincideBusqueda = q.isBlank() ||
                    producto.codigo.lowercase().contains(q) ||
                    producto.nombre.lowercase().contains(q) ||
                    producto.descripcion?.lowercase()?.contains(q) == true
                val coincideFiltro = when (filtro) {
                    FiltroProductos.TODOS -> true
                    FiltroProductos.STOCK_BAJO -> producto.stockBajo
                    FiltroProductos.INACTIVOS -> !producto.activo
                }
                coincideBusqueda && coincideFiltro
            }
        }

    val totalStockBajo: Int
        get() = productos.count { it.stockBajo }

    val totalInactivos: Int
        get() = productos.count { !it.activo }
}