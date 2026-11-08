package com.maranathastore.app.presentation.producto

import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.domain.ErrorApi
import com.maranathastore.app.domain.ErrorDeApi
import com.maranathastore.app.domain.auth.Rol
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.domain.producto.Precio
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.support.AlmacenSeguroFalso
import com.maranathastore.app.support.ProductoRepositoryFalso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var gestor: GestorSesion
    private lateinit var repositorio: ProductoRepositoryFalso

    private val productoActivo = Producto(
        id = 1,
        codigo = "ART-1",
        nombre = "Biblia",
        descripcion = "RVR",
        precio = Precio.desdeTexto("49.90")!!,
        stock = 12,
        activo = true
    )
    private val productoBajo = Producto(
        id = 2,
        codigo = "ART-2",
        nombre = "Lapicero",
        descripcion = null,
        precio = Precio.desdeTexto("2.00")!!,
        stock = 3,
        activo = true
    )
    private val productoInactivo = Producto(
        id = 3,
        codigo = "ART-3",
        nombre = "Agenda",
        descripcion = "2026",
        precio = Precio.desdeTexto("15.00")!!,
        stock = 0,
        activo = false
    )

    @BeforeTest
    fun preparar() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        gestor = GestorSesion(AlmacenSeguroFalso(Sesion("jwt", "admin", Rol.ADMIN)))
        repositorio = ProductoRepositoryFalso(listOf(productoActivo, productoBajo, productoInactivo))
    }

    @AfterTest
    fun limpiar() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carga productos y marca el estado como listo`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val estado = viewModel.uiState.value
        assertFalse(estado.cargando)
        assertEquals(3, estado.productos.size)
        assertNull(estado.errorCarga)
        assertTrue(estado.esAdmin)
    }

    @Test
    fun `busqueda filtra por nombre y descripcion`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.onBusquedaChange("RVR")
        assertEquals(1, viewModel.uiState.value.visibles.size)
        viewModel.onBusquedaChange("lap")
        assertEquals(1, viewModel.uiState.value.visibles.size)
        viewModel.onBusquedaChange("inexistente")
        assertEquals(0, viewModel.uiState.value.visibles.size)
    }

    @Test
    fun `filtro stock bajo muestra solo productos con stock bajo`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.onFiltroChange(FiltroProductos.STOCK_BAJO)
        assertEquals(2, viewModel.uiState.value.visibles.size)
    }

    @Test
    fun `filtro inactivos muestra solo inactivos`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.onFiltroChange(FiltroProductos.INACTIVOS)
        assertEquals(1, viewModel.uiState.value.visibles.size)
        assertEquals("Agenda", viewModel.uiState.value.visibles[0].nombre)
    }

    @Test
    fun `usuario no admin no puede abrir el formulario`() = runTest(dispatcher) {
        val gestorUsuario = GestorSesion(AlmacenSeguroFalso(Sesion("jwt", "caja", Rol.USER)))
        val viewModel = ProductoViewModel(repositorio, gestorUsuario)
        advanceUntilIdle()
        viewModel.abrirCrear()
        assertNull(viewModel.uiState.value.formulario)
        viewModel.abrirEditar(viewModel.uiState.value.productos[0])
        assertNull(viewModel.uiState.value.formulario)
    }

    @Test
    fun `crear producto valido recarga la lista`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("ART-4")
        viewModel.onFormNombreChange("Cuaderno")
        viewModel.onFormPrecioChange("3.50")
        viewModel.onFormStockChange("10")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.formulario)
        assertIs<AvisoUi.Exito>(viewModel.uiState.value.aviso)
        assertEquals("¡Artículo registrado con éxito!", (viewModel.uiState.value.aviso as AvisoUi.Exito).mensaje)
        assertEquals(4, viewModel.uiState.value.productos.size)
        assertEquals(1, repositorio.crearLlamadas)
    }

    @Test
    fun `guardar con datos invalidos no llama al backend`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormNombreChange("   ")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.formulario)
        assertEquals(0, repositorio.crearLlamadas)
        assertTrue(viewModel.uiState.value.formulario!!.errores.nombre != null)
    }

    @Test
    fun `guardar con codigo duplicado no llama al backend y muestra error`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("ART-1") // ya existe en productoActivo
        viewModel.onFormNombreChange("Nuevo libro")
        viewModel.onFormPrecioChange("15.00")
        viewModel.onFormStockChange("5")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.formulario)
        assertEquals(0, repositorio.crearLlamadas)
        assertEquals(
            "El código 'ART-1' ya está registrado en otro artículo.",
            viewModel.uiState.value.formulario!!.errores.codigo
        )
    }

    @Test
    fun `guardar con datos incompletos marca errores en campos obligatorios`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("")
        viewModel.onFormNombreChange("")
        viewModel.onFormPrecioChange("")
        viewModel.onFormStockChange("")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        val errores = viewModel.uiState.value.formulario!!.errores
        assertEquals("El código del artículo es obligatorio.", errores.codigo)
        assertEquals("El nombre del artículo es obligatorio.", errores.nombre)
        assertEquals("Ingresa el precio del artículo.", errores.precio)
        assertEquals("Ingresa el stock inicial.", errores.stock)
        assertEquals(0, repositorio.crearLlamadas)
    }

    @Test
    fun `guardar con precio cero o menor muestra error`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("COD-99")
        viewModel.onFormNombreChange("Folleto")
        viewModel.onFormPrecioChange("0")
        viewModel.onFormStockChange("10")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertEquals("El precio debe ser mayor que 0.", viewModel.uiState.value.formulario!!.errores.precio)
        assertEquals(0, repositorio.crearLlamadas)
    }

    @Test
    fun `guardar con stock negativo muestra error`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("COD-99")
        viewModel.onFormNombreChange("Folleto")
        viewModel.onFormPrecioChange("5.00")
        viewModel.onFormStockChange("-2")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertEquals("El stock no puede ser negativo.", viewModel.uiState.value.formulario!!.errores.stock)
        assertEquals(0, repositorio.crearLlamadas)
    }

    @Test
    fun `guardar bloquea doble envio`() = runTest(dispatcher) {
        repositorio.fallo = null
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.abrirCrear()
        viewModel.onFormCodigoChange("ART-4")
        viewModel.onFormNombreChange("Cuaderno")
        viewModel.onFormPrecioChange("3.50")
        viewModel.onFormStockChange("10")
        viewModel.guardarFormulario()
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertEquals(1, repositorio.crearLlamadas)
    }

    @Test
    fun `editar producto usa stock fresco del backend`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[0]
        viewModel.abrirEditar(producto)
        viewModel.onFormNombreChange("Biblia NTV")
        viewModel.onFormPrecioChange("55.00")
        viewModel.guardarFormulario()
        advanceUntilIdle()
        assertEquals(1, repositorio.actualizarLlamadas)
        assertEquals(12, repositorio.ultimaActualizacion?.second?.stock)
        assertEquals("Biblia NTV", repositorio.ultimaActualizacion?.second?.nombre)
    }

    @Test
    fun `eliminar producto quita la fila`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[0]
        viewModel.solicitarEliminar(producto)
        assertNotNull(viewModel.uiState.value.confirmacionEliminar)
        viewModel.confirmarEliminar()
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.productos.size)
        assertEquals(1, repositorio.eliminarLlamadas)
        assertNull(viewModel.uiState.value.confirmacionEliminar)
        assertIs<AvisoUi.Exito>(viewModel.uiState.value.aviso)
    }

    @Test
    fun `eliminar fallido mantiene el dialogo con error`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        repositorio.fallo = ErrorDeApi(ErrorApi.Conflicto("Tiene ventas"))
        val producto = viewModel.uiState.value.productos[0]
        viewModel.solicitarEliminar(producto)
        viewModel.confirmarEliminar()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.confirmacionEliminar)
        assertEquals("Tiene ventas", viewModel.uiState.value.confirmacionEliminar?.error)
        assertEquals(3, viewModel.uiState.value.productos.size)
    }

    @Test
    fun `reponer stock actualiza la fila con datos del backend`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[1]
        viewModel.abrirAjusteStock(producto)
        viewModel.onAjusteOperacionChange(OperacionStock.REPONER)
        viewModel.onAjusteCantidadChange("5")
        viewModel.confirmarAjusteStock()
        advanceUntilIdle()
        val actualizado = viewModel.uiState.value.productos.first { it.id == producto.id }
        assertEquals(8, actualizado.stock)
        assertEquals(1, repositorio.reponerLlamadas)
        assertNull(viewModel.uiState.value.ajusteStock)
    }

    @Test
    fun `descontar mas del stock disponible se bloquea localmente`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[1]
        viewModel.abrirAjusteStock(producto)
        viewModel.onAjusteOperacionChange(OperacionStock.DESCONTAR)
        viewModel.onAjusteCantidadChange("10")
        viewModel.confirmarAjusteStock()
        advanceUntilIdle()
        assertEquals(0, repositorio.descontarLlamadas)
        assertNotNull(viewModel.uiState.value.ajusteStock)
        assertTrue(viewModel.uiState.value.ajusteStock!!.error?.contains("No puedes descontar") == true)
    }

    @Test
    fun `descontar stock valido actualiza la fila`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[1]
        viewModel.abrirAjusteStock(producto)
        viewModel.onAjusteOperacionChange(OperacionStock.DESCONTAR)
        viewModel.onAjusteCantidadChange("2")
        viewModel.confirmarAjusteStock()
        advanceUntilIdle()
        val actualizado = viewModel.uiState.value.productos.first { it.id == producto.id }
        assertEquals(1, actualizado.stock)
        assertEquals(1, repositorio.descontarLlamadas)
    }

    @Test
    fun `producto inactivo no permite descontar`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        val producto = viewModel.uiState.value.productos[2]
        viewModel.abrirAjusteStock(producto)
        assertEquals(OperacionStock.REPONER, viewModel.uiState.value.ajusteStock?.operacion)
        viewModel.onAjusteOperacionChange(OperacionStock.DESCONTAR)
        assertEquals(OperacionStock.REPONER, viewModel.uiState.value.ajusteStock?.operacion)
    }

    @Test
    fun `error de carga se reporta cuando no hay productos`() = runTest(dispatcher) {
        repositorio.fallo = ErrorDeApi(ErrorApi.SinConexion("sin conexion"))
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        assertEquals("No se pudo conectar con el servidor. Verifica tu conexión.", viewModel.uiState.value.errorCarga)
        assertTrue(viewModel.uiState.value.productos.isEmpty())
    }

    @Test
    fun `limpiar aviso borra el mensaje`() = runTest(dispatcher) {
        val viewModel = ProductoViewModel(repositorio, gestor)
        advanceUntilIdle()
        viewModel.solicitarEliminar(viewModel.uiState.value.productos[0])
        viewModel.confirmarEliminar()
        advanceUntilIdle()
        viewModel.limpiarAviso()
        assertNull(viewModel.uiState.value.aviso)
    }
}