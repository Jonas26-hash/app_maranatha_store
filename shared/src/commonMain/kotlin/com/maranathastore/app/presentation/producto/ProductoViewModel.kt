package com.maranathastore.app.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.mensajeParaUsuario
import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.producto.ErroresProducto
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.domain.producto.ValidacionProducto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val repositorio: ProductoRepository,
    gestorSesion: GestorSesion
) : ViewModel() {

    private val esAdmin: Boolean = gestorSesion.sesion.value?.rol == Rol.ADMIN

    private val _uiState = MutableStateFlow(ProductosUiState(esAdmin = esAdmin))
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        if (_uiState.value.cargando && _uiState.value.productos.isNotEmpty()) return
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            try {
                val productos = repositorio.listar().map { it.aUi() }
                _uiState.update {
                    it.copy(cargando = false, productos = productos, errorCarga = null)
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                val mensaje = mensajeDeError(fallo)
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorCarga = if (it.productos.isEmpty()) mensaje else it.errorCarga,
                        aviso = if (it.productos.isEmpty()) it.aviso else AvisoUi.Error(mensaje)
                    )
                }
            }
        }
    }

    fun onBusquedaChange(valor: String) {
        _uiState.update { it.copy(busqueda = valor) }
    }

    fun onFiltroChange(filtro: FiltroProductos) {
        _uiState.update { it.copy(filtro = filtro) }
    }

    fun limpiarAviso() {
        _uiState.update { it.copy(aviso = null) }
    }

    fun abrirCrear() {
        if (!esAdmin) return
        _uiState.update {
            it.copy(formulario = ProductoFormulario(ModoFormulario.CREAR))
        }
    }

    fun abrirEditar(producto: ProductoUi) {
        if (!esAdmin) return
        _uiState.update {
            it.copy(
                formulario = ProductoFormulario(
                    modo = ModoFormulario.EDITAR,
                    id = producto.id,
                    codigo = producto.codigo,
                    nombre = producto.nombre,
                    descripcion = producto.descripcion.orEmpty(),
                    precio = producto.precioTexto,
                    stock = producto.stock.toString(),
                    activo = producto.activo
                )
            )
        }
    }

    fun cerrarFormulario() {
        val form = _uiState.value.formulario
        if (form?.guardando == true) return
        _uiState.update { it.copy(formulario = null) }
    }

    fun onFormCodigoChange(valor: String) =
        actualizarFormulario { it.copy(codigo = valor) }

    fun onFormNombreChange(valor: String) =
        actualizarFormulario { it.copy(nombre = valor) }

    fun onFormDescripcionChange(valor: String) =
        actualizarFormulario { it.copy(descripcion = valor) }

    fun onFormPrecioChange(valor: String) =
        actualizarFormulario { it.copy(precio = valor) }

    fun onFormStockChange(valor: String) =
        actualizarFormulario { it.copy(stock = valor) }

    fun onFormActivoChange(valor: Boolean) =
        actualizarFormulario { it.copy(activo = valor) }

    fun guardarFormulario() {
        val form = _uiState.value.formulario ?: return
        if (form.guardando || !esAdmin) return

        val codigosExistentes = _uiState.value.productos
            .filter { p -> if (form.esEdicion) p.id != form.id else true }
            .map { it.codigo }
            .toSet()

        val resultado = ValidacionProducto.validar(
            codigo = form.codigo,
            nombre = form.nombre,
            descripcion = form.descripcion,
            precioTexto = form.precio,
            stockTexto = form.stock,
            activo = form.activo,
            codigosExistentes = codigosExistentes
        )

        val datos = when (resultado) {
            is com.maranathastore.app.domain.producto.ResultadoValidacionProducto.Invalido -> {
                _uiState.update { estado ->
                    estado.copy(formulario = form.copy(errores = resultado.errores))
                }
                return
            }
            is com.maranathastore.app.domain.producto.ResultadoValidacionProducto.Valido -> resultado.datos
        }

        _uiState.update { estado ->
            estado.copy(formulario = form.copy(guardando = true, errores = ErroresProducto(), errorServidor = null))
        }

        viewModelScope.launch {
            try {
                if (form.esEdicion) {
                    val actual = repositorio.obtener(requireNotNull(form.id))
                    repositorio.actualizar(actual.id, datos.copy(stock = actual.stock))
                } else {
                    repositorio.crear(datos)
                }
                _uiState.update { estado ->
                    estado.copy(
                        formulario = null,
                        aviso = AvisoUi.Exito(
                            if (form.esEdicion) "Artículo actualizado con éxito." else "¡Artículo registrado con éxito!"
                        )
                    )
                }
                refrescarLista()
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                _uiState.update { estado ->
                    estado.copy(
                        formulario = estado.formulario?.copy(
                            guardando = false,
                            errorServidor = mensajeDeError(fallo)
                        )
                    )
                }
            }
        }
    }

    fun solicitarEliminar(producto: ProductoUi) {
        if (!esAdmin) return
        _uiState.update {
            it.copy(confirmacionEliminar = ConfirmacionEliminar(producto = producto))
        }
    }

    fun cancelarEliminar() {
        val confirmacion = _uiState.value.confirmacionEliminar
        if (confirmacion?.eliminando == true) return
        _uiState.update { it.copy(confirmacionEliminar = null) }
    }

    fun confirmarEliminar() {
        val confirmacion = _uiState.value.confirmacionEliminar ?: return
        if (!esAdmin || confirmacion.eliminando) return

        _uiState.update {
            it.copy(confirmacionEliminar = confirmacion.copy(eliminando = true, error = null))
        }

        viewModelScope.launch {
            try {
                repositorio.eliminar(confirmacion.producto.id)
                _uiState.update { estado ->
                    estado.copy(
                        productos = estado.productos.filter { it.id != confirmacion.producto.id },
                        confirmacionEliminar = null,
                        aviso = AvisoUi.Exito("Producto eliminado.")
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                _uiState.update { estado ->
                    estado.copy(
                        confirmacionEliminar = estado.confirmacionEliminar?.copy(
                            eliminando = false,
                            error = mensajeDeError(fallo)
                        )
                    )
                }
            }
        }
    }

    fun abrirAjusteStock(producto: ProductoUi) {
        if (!esAdmin) return
        _uiState.update {
            it.copy(
                ajusteStock = AjusteStock(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    stockActual = producto.stock,
                    activo = producto.activo
                )
            )
        }
    }

    fun cerrarAjusteStock() {
        val ajuste = _uiState.value.ajusteStock
        if (ajuste?.guardando == true) return
        _uiState.update { it.copy(ajusteStock = null) }
    }

    fun onAjusteOperacionChange(operacion: OperacionStock) {
        val ajuste = _uiState.value.ajusteStock ?: return
        if (ajuste.guardando) return
        if (!ajuste.activo && operacion == OperacionStock.DESCONTAR) return
        _uiState.update {
            it.copy(ajusteStock = ajuste.copy(operacion = operacion, error = null))
        }
    }

    fun onAjusteCantidadChange(valor: String) {
        val ajuste = _uiState.value.ajusteStock ?: return
        if (ajuste.guardando) return
        _uiState.update {
            it.copy(ajusteStock = ajuste.copy(cantidad = valor, error = null))
        }
    }

    fun confirmarAjusteStock() {
        val ajuste = _uiState.value.ajusteStock ?: return
        if (!esAdmin || ajuste.guardando) return

        when (val resultado = ValidacionProducto.validarCantidad(ajuste.cantidad)) {
            is com.maranathastore.app.domain.producto.ResultadoCantidad.Invalida -> {
                _uiState.update {
                    it.copy(ajusteStock = ajuste.copy(error = resultado.mensaje))
                }
                return
            }

            is com.maranathastore.app.domain.producto.ResultadoCantidad.Valida -> {
                // continúa
            }
        }

        val cantidad = (ValidacionProducto.validarCantidad(ajuste.cantidad)
            as com.maranathastore.app.domain.producto.ResultadoCantidad.Valida).cantidad

        if (ajuste.operacion == OperacionStock.DESCONTAR) {
            if (!ajuste.activo) {
                _uiState.update {
                    it.copy(ajusteStock = ajuste.copy(error = "El producto está inactivo."))
                }
                return
            }
            if (cantidad > ajuste.stockActual) {
                _uiState.update {
                    it.copy(
                        ajusteStock = ajuste.copy(
                            error = "No puedes descontar más de ${ajuste.stockActual} unidades."
                        )
                    )
                }
                return
            }
        }

        _uiState.update {
            it.copy(ajusteStock = ajuste.copy(guardando = true, error = null))
        }

        viewModelScope.launch {
            try {
                val actualizado = when (ajuste.operacion) {
                    OperacionStock.REPONER -> repositorio.reponerStock(ajuste.productoId, cantidad)
                    OperacionStock.DESCONTAR -> repositorio.descontarStock(ajuste.productoId, cantidad)
                }
                _uiState.update { estado ->
                    estado.copy(
                        productos = estado.productos.map { producto ->
                            if (producto.id == actualizado.id) actualizado.aUi() else producto
                        },
                        ajusteStock = null,
                        aviso = AvisoUi.Exito("Stock actualizado: ${actualizado.stock} unidades.")
                    )
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (fallo: Throwable) {
                _uiState.update { estado ->
                    estado.copy(
                        ajusteStock = estado.ajusteStock?.copy(
                            guardando = false,
                            error = mensajeDeError(fallo)
                        )
                    )
                }
            }
        }
    }

    private fun actualizarFormulario(transform: (ProductoFormulario) -> ProductoFormulario) {
        _uiState.update { estado ->
            estado.formulario?.let { form ->
                estado.copy(formulario = transform(form).copy(errores = ErroresProducto(), errorServidor = null))
            } ?: estado
        }
    }

    private suspend fun refrescarLista() {
        try {
            val productos = repositorio.listar().map { it.aUi() }
            _uiState.update {
                it.copy(cargando = false, productos = productos, errorCarga = null)
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (fallo: Throwable) {
            _uiState.update {
                it.copy(aviso = AvisoUi.Error(mensajeDeError(fallo)))
            }
        }
    }
}

private fun mensajeDeError(fallo: Throwable): String = when (fallo) {
    is ErrorDeApi -> fallo.error.mensajeParaUsuario()
    else -> "No se pudo completar la operación. Inténtalo de nuevo."
}