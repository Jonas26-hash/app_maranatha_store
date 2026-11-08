package com.maranathastore.app.presentation.producto

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassChip
import com.maranathastore.app.presentation.components.GlassEmptyState
import com.maranathastore.app.presentation.components.GlassErrorState
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassLoading
import com.maranathastore.app.presentation.components.GlassPill
import com.maranathastore.app.presentation.components.GlassSearchBar
import com.maranathastore.app.presentation.components.GlassTextField
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(estado.aviso) {
        val aviso = estado.aviso ?: return@LaunchedEffect
        viewModel.limpiarAviso()
        snackbarHostState.showSnackbar(aviso.mensaje)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassSearchBar(
                    value = estado.busqueda,
                    onValueChange = viewModel::onBusquedaChange,
                    placeholder = "Buscar por nombre o descripción"
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FiltroProductos.entries.forEach { filtro ->
                        val cantidad = when (filtro) {
                            FiltroProductos.TODOS -> estado.productos.size
                            FiltroProductos.STOCK_BAJO -> estado.totalStockBajo
                            FiltroProductos.INACTIVOS -> estado.totalInactivos
                        }
                        GlassChip(
                            label = "${filtro.etiqueta} · $cantidad",
                            selected = estado.filtro == filtro,
                            onClick = { viewModel.onFiltroChange(filtro) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${estado.productos.size} productos",
                            style = MaterialTheme.typography.titleSmall,
                            color = LiquidGlass.Texto
                        )
                        if (estado.totalStockBajo > 0) {
                            Text(
                                text = "${estado.totalStockBajo} con stock bajo",
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidGlass.Ambar
                            )
                        }
                    }
                    if (estado.cargando) {
                        GlassLoading()
                    }
                    IconButton(
                        onClick = { viewModel.cargar() },
                        enabled = !estado.cargando
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = LiquidGlass.TextoSuave
                        )
                    }
                    if (estado.esAdmin) {
                        Button(
                            onClick = { viewModel.abrirCrear() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidGlass.Esmeralda,
                                contentColor = Color(0xFF00271A)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Nuevo")
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    estado.cargando && estado.productos.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GlassLoading()
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Cargando productos…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LiquidGlass.TextoSuave
                            )
                        }
                    }

                    estado.errorCarga != null && estado.productos.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = LiquidGlass.Peligro,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            GlassErrorState(
                                message = estado.errorCarga
                                    ?: "No se pudieron cargar los productos."
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.cargar() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LiquidGlass.Esmeralda,
                                    contentColor = Color(0xFF00271A)
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }

                    estado.visibles.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GlassIconTile(
                                icon = if (estado.productos.isEmpty()) {
                                    Icons.Default.Inventory2
                                } else {
                                    Icons.Default.SearchOff
                                },
                                tint = LiquidGlass.Azul,
                                container = LiquidGlass.Azul.copy(alpha = 0.12f),
                                size = 56
                            )
                            Spacer(Modifier.height(14.dp))
                            GlassEmptyState(
                                titulo = if (estado.productos.isEmpty()) {
                                    "Aún no hay productos"
                                } else {
                                    "Sin resultados"
                                },
                                message = if (estado.productos.isEmpty()) {
                                    "Registra tu primer artículo para comenzar."
                                } else {
                                    "No hay resultados para la búsqueda o el filtro."
                                }
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = estado.visibles,
                                key = { _, producto -> producto.id }
                            ) { indice, producto ->
                                Reveal(delayMillis = (indice.coerceAtMost(9)) * 35) {
                                    ProductoCard(
                                        producto = producto,
                                        esAdmin = estado.esAdmin,
                                        onEditar = { viewModel.abrirEditar(producto) },
                                        onAjustarStock = { viewModel.abrirAjusteStock(producto) },
                                        onEliminar = { viewModel.solicitarEliminar(producto) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    estado.formulario?.let { formulario ->
        ProductoFormularioDialog(
            formulario = formulario,
            onCerrar = { viewModel.cerrarFormulario() },
            onCodigoChange = viewModel::onFormCodigoChange,
            onNombreChange = viewModel::onFormNombreChange,
            onDescripcionChange = viewModel::onFormDescripcionChange,
            onPrecioChange = viewModel::onFormPrecioChange,
            onStockChange = viewModel::onFormStockChange,
            onActivoChange = viewModel::onFormActivoChange,
            onGuardar = { viewModel.guardarFormulario() }
        )
    }

    estado.ajusteStock?.let { ajuste ->
        AjusteStockDialog(
            ajuste = ajuste,
            onCerrar = { viewModel.cerrarAjusteStock() },
            onOperacionChange = viewModel::onAjusteOperacionChange,
            onCantidadChange = viewModel::onAjusteCantidadChange,
            onConfirmar = { viewModel.confirmarAjusteStock() }
        )
    }

    estado.confirmacionEliminar?.let { confirmacion ->
        EliminarProductoDialog(
            confirmacion = confirmacion,
            onCancelar = { viewModel.cancelarEliminar() },
            onConfirmar = { viewModel.confirmarEliminar() }
        )
    }
}

@Composable
private fun ProductoCard(
    producto: ProductoUi,
    esAdmin: Boolean,
    onEditar: () -> Unit,
    onAjustarStock: () -> Unit,
    onEliminar: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (producto.activo) LiquidGlass.VerdeNeon else LiquidGlass.Azul,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassIconTile(
                    icon = Icons.Default.Inventory2,
                    tint = if (producto.activo) LiquidGlass.VerdeNeon else LiquidGlass.TextoSuave,
                    container = if (producto.activo) {
                        LiquidGlass.VerdeNeon.copy(alpha = 0.12f)
                    } else {
                        LiquidGlass.CristalSuave
                    },
                    size = 42
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GlassPill(
                            text = producto.codigo,
                            color = LiquidGlass.Azul,
                            container = LiquidGlass.Azul.copy(alpha = 0.12f)
                        )
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            color = LiquidGlass.Texto,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    producto.descripcion?.let { descripcion ->
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (esAdmin) {
                    Box {
                        IconButton(onClick = { menuAbierto = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Acciones",
                                tint = LiquidGlass.TextoSuave
                            )
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = {
                                    menuAbierto = false
                                    onEditar()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Ajustar stock") },
                                onClick = {
                                    menuAbierto = false
                                    onAjustarStock()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Eliminar",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuAbierto = false
                                    onEliminar()
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = producto.precio,
                    style = MaterialTheme.typography.titleLarge,
                    color = LiquidGlass.VerdeNeon
                )
                Spacer(Modifier.weight(1f))
                EtiquetaStock(producto)
                if (!producto.activo) {
                    GlassPill(
                        text = "Inactivo",
                        color = LiquidGlass.TextoSuave,
                        container = LiquidGlass.CristalSuave
                    )
                }
            }
        }
    }
}

@Composable
private fun EtiquetaStock(producto: ProductoUi) {
    val (tono, texto) = when {
        producto.stock <= 0 -> LiquidGlass.Peligro to "Agotado"
        producto.stockBajo -> LiquidGlass.Ambar to "Stock ${producto.stock}"
        else -> LiquidGlass.Azul to "Stock ${producto.stock}"
    }

    GlassPill(
        text = texto,
        color = tono,
        container = tono.copy(alpha = 0.12f)
    )
}

@Composable
private fun ProductoFormularioDialog(
    formulario: ProductoFormulario,
    onCerrar: () -> Unit,
    onCodigoChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onActivoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit
) {
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(16.dp),
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = formulario.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = LiquidGlass.Texto
                )

                GlassTextField(
                    value = formulario.codigo,
                    onValueChange = onCodigoChange,
                    label = "Código del artículo *",
                    placeholder = "Ej. ART-001",
                    enabled = !formulario.guardando && !formulario.esEdicion,
                    isError = formulario.errores.codigo != null,
                    supportingText = formulario.errores.codigo
                )

                GlassTextField(
                    value = formulario.nombre,
                    onValueChange = onNombreChange,
                    label = "Nombre del artículo *",
                    placeholder = "Ej. Biblia Reina Valera 1960",
                    enabled = !formulario.guardando,
                    isError = formulario.errores.nombre != null,
                    supportingText = formulario.errores.nombre
                )

                GlassTextField(
                    value = formulario.descripcion,
                    onValueChange = onDescripcionChange,
                    label = "Descripción (opcional)",
                    placeholder = "Detalles del artículo...",
                    enabled = !formulario.guardando,
                    isError = formulario.errores.descripcion != null,
                    supportingText = formulario.errores.descripcion,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4
                )

                GlassTextField(
                    value = formulario.precio,
                    onValueChange = onPrecioChange,
                    label = "Precio (S/) *",
                    placeholder = "Ej. 45.00",
                    enabled = !formulario.guardando,
                    isError = formulario.errores.precio != null,
                    supportingText = formulario.errores.precio,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                if (!formulario.esEdicion) {
                    GlassTextField(
                        value = formulario.stock,
                        onValueChange = onStockChange,
                        label = "Stock inicial *",
                        placeholder = "0 o mayor",
                        enabled = !formulario.guardando,
                        isError = formulario.errores.stock != null,
                        supportingText = formulario.errores.stock,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                } else {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = LiquidGlass.Azul,
                        contentPadding = PaddingValues(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "Stock actual: ${formulario.stock}",
                                style = MaterialTheme.typography.titleSmall,
                                color = LiquidGlass.Texto
                            )
                            Text(
                                text = "Los cambios de stock se realizan con “Ajustar stock” " +
                                    "para no sobrescribir ventas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidGlass.TextoSuave
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Activo",
                            style = MaterialTheme.typography.titleSmall,
                            color = LiquidGlass.Texto
                        )
                        Text(
                            text = if (formulario.activo) {
                                "Visible para ventas."
                            } else {
                                "Oculto para ventas."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave
                        )
                    }
                    Switch(
                        checked = formulario.activo,
                        onCheckedChange = onActivoChange,
                        enabled = !formulario.guardando
                    )
                }

                formulario.errorServidor?.let { mensaje ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = LiquidGlass.Peligro.copy(alpha = 0.16f),
                        contentColor = LiquidGlass.Peligro
                    ) {
                        Text(
                            text = mensaje,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    TextButton(
                        onClick = onCerrar,
                        enabled = !formulario.guardando
                    ) {
                        Text("Cancelar", color = LiquidGlass.TextoSuave)
                    }
                    Button(
                        onClick = onGuardar,
                        enabled = formulario.puedeGuardar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidGlass.Esmeralda,
                            contentColor = Color(0xFF00271A),
                            disabledContainerColor = LiquidGlass.CristalSuave,
                            disabledContentColor = LiquidGlass.TextoSuave
                        )
                    ) {
                        if (formulario.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00271A)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Guardando…")
                        } else {
                            Text("Guardar artículo")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AjusteStockDialog(
    ajuste: AjusteStock,
    onCerrar: () -> Unit,
    onOperacionChange: (OperacionStock) -> Unit,
    onCantidadChange: (String) -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        containerColor = LiquidGlass.SuperficieDialogo,
        titleContentColor = LiquidGlass.Texto,
        textContentColor = LiquidGlass.TextoSuave,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Ajustar stock") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = ajuste.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = "Stock actual: ${ajuste.stockActual}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(
                        label = "Reponer (+)",
                        selected = ajuste.operacion == OperacionStock.REPONER,
                        onClick = { onOperacionChange(OperacionStock.REPONER) },
                        enabled = !ajuste.guardando
                    )
                    GlassChip(
                        label = "Descontar (−)",
                        selected = ajuste.operacion == OperacionStock.DESCONTAR,
                        onClick = { onOperacionChange(OperacionStock.DESCONTAR) },
                        enabled = !ajuste.guardando && ajuste.activo
                    )
                }
                if (!ajuste.activo) {
                    Text(
                        text = "El producto está inactivo: solo se puede reponer stock.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                GlassTextField(
                    value = ajuste.cantidad,
                    onValueChange = onCantidadChange,
                    label = "Cantidad",
                    enabled = !ajuste.guardando,
                    isError = ajuste.error != null,
                    supportingText = ajuste.error,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (ajuste.operacion == OperacionStock.DESCONTAR && ajuste.activo) {
                    Text(
                        text = "No puedes descontar más de ${ajuste.stockActual} unidades.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !ajuste.guardando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGlass.Esmeralda,
                    contentColor = Color(0xFF00271A)
                )
            ) {
                if (ajuste.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF00271A)
                    )
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCerrar,
                enabled = !ajuste.guardando
            ) {
                Text("Cancelar", color = LiquidGlass.TextoSuave)
            }
        }
    )
}

@Composable
private fun EliminarProductoDialog(
    confirmacion: ConfirmacionEliminar,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = LiquidGlass.SuperficieDialogo,
        titleContentColor = LiquidGlass.Texto,
        textContentColor = LiquidGlass.TextoSuave,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Eliminar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "¿Eliminar «${confirmacion.producto.nombre}»?",
                    color = LiquidGlass.Texto
                )
                Text(
                    text = "Esta acción es permanente en el backend. Si el producto tiene " +
                        "ventas relacionadas, el servidor puede rechazarla.",
                    style = MaterialTheme.typography.bodySmall
                )
                confirmacion.error?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LiquidGlass.Peligro.copy(alpha = 0.16f),
                        contentColor = LiquidGlass.Peligro
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !confirmacion.eliminando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LiquidGlass.Peligro,
                    contentColor = Color(0xFF2A060C)
                )
            ) {
                if (confirmacion.eliminando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF2A060C)
                    )
                } else {
                    Text("Eliminar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                enabled = !confirmacion.eliminando
            ) {
                Text("Cancelar", color = LiquidGlass.TextoSuave)
            }
        }
    )
}
