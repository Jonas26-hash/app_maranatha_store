package com.maranathastore.app.presentation.ventas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassChip
import com.maranathastore.app.presentation.components.GlassEmptyState
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassPill
import com.maranathastore.app.presentation.components.GlassSearchBar
import com.maranathastore.app.presentation.components.GlassSectionHeader
import com.maranathastore.app.presentation.components.GlassTextField
import com.maranathastore.app.theme.LiquidGlass
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.round

@Composable
fun NuevaVentaScreen() {
    val vm = koinViewModel<NuevaVentaViewModel>()

    val productos by vm.productos.collectAsStateWithLifecycle()
    val clientes by vm.clientes.collectAsStateWithLifecycle()
    val carrito by vm.carrito.collectAsStateWithLifecycle()
    val tipoComprobante by vm.tipoComprobante.collectAsStateWithLifecycle()
    val clienteId by vm.clienteId.collectAsStateWithLifecycle()

    var busqueda by remember { mutableStateOf("") }
    var productoParaCantidad by remember { mutableStateOf<Producto?>(null) }
    var mostrarConfirmarVenta by remember { mutableStateOf(false) }

    val clienteSeleccionado = clientes.firstOrNull { it.id == clienteId }
    val total = carrito.sumOf { (producto, cantidad) -> producto.precio.comoDouble * cantidad }
    val unidades = carrito.sumOf { it.second }

    val filtrados = productos.filter { producto ->
        producto.activo && (
            busqueda.isBlank() ||
                producto.nombre.lowercase().contains(busqueda.trim().lowercase()) ||
                producto.codigo.lowercase().contains(busqueda.trim().lowercase())
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                GlassSearchBar(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    placeholder = "Buscar producto para agregar"
                )
            }

            item {
                GlassSectionHeader(
                    titulo = "Catálogo",
                    detalle = "${filtrados.size} productos disponibles",
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (filtrados.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GlassIconTile(
                            icon = Icons.Default.SearchOff,
                            tint = LiquidGlass.Azul,
                            container = LiquidGlass.Azul.copy(alpha = 0.12f),
                            size = 52
                        )
                        Spacer(Modifier.height(12.dp))
                        GlassEmptyState(
                            titulo = "Sin productos",
                            message = "No hay productos activos que coincidan con la búsqueda."
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = filtrados,
                    key = { _, producto -> producto.id }
                ) { indice, producto ->
                    ProductoVentaRow(
                        producto = producto,
                        onAgregarClick = { productoParaCantidad = producto }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = carrito.isNotEmpty(),
            enter = fadeIn() + slideInVertically { alto -> alto },
            exit = fadeOut() + slideOutVertically { alto -> alto }
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                glowColor = LiquidGlass.VerdeNeon,
                borderBrush = androidx.compose.ui.graphics.Brush.linearGradient(
                    listOf(
                        LiquidGlass.VerdeNeon.copy(alpha = 0.55f),
                        LiquidGlass.Azul.copy(alpha = 0.30f)
                    )
                ),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = LiquidGlass.VerdeNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Carrito",
                            style = MaterialTheme.typography.titleSmall,
                            color = LiquidGlass.Texto,
                            modifier = Modifier.weight(1f)
                        )
                        GlassPill(text = "$unidades und.")
                    }

                    carrito.forEachIndexed { index, (producto, cantidad) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${cantidad}× ${producto.nombre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidGlass.TextoSuave,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = producto.precio.formateado(),
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidGlass.Texto
                            )
                            IconButton(
                                onClick = { vm.quitarDelCarrito(producto) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Quitar ${producto.nombre}",
                                    tint = LiquidGlass.Peligro.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = LiquidGlass.Borde.copy(alpha = 0.25f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleSmall,
                            color = LiquidGlass.TextoSuave,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "S/ ${formatearMonto(total)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = LiquidGlass.VerdeNeon
                        )
                    }

                    Button(
                        onClick = { mostrarConfirmarVenta = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidGlass.Esmeralda,
                            contentColor = Color(0xFF00271A)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Emitir venta")
                    }
                }
            }
        }

        productoParaCantidad?.let { producto ->
            Dialog(
                onDismissRequest = { productoParaCantidad = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.45f))
                ) {
                    ModalAgregarCantidadSheet(
                        producto = producto,
                        onConfirmar = { cantidad ->
                            vm.agregar(producto, cantidad)
                            productoParaCantidad = null
                        },
                        onCancelar = { productoParaCantidad = null }
                    )
                }
            }
        }

        if (mostrarConfirmarVenta) {
            Dialog(
                onDismissRequest = { mostrarConfirmarVenta = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.45f))
                ) {
                    ModalConfirmarVentaSheet(
                        clientes = clientes,
                        clienteId = clienteId,
                        tipoComprobante = tipoComprobante,
                        onClienteChange = { vm.clienteId.value = it },
                        onTipoComprobanteChange = { vm.tipoComprobante.value = it },
                        total = total,
                        unidades = unidades,
                        onConfirmar = {
                            vm.enviar()
                            mostrarConfirmarVenta = false
                        },
                        onCancelar = { mostrarConfirmarVenta = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModalAgregarCantidadSheet(
    producto: Producto,
    onConfirmar: (Int) -> Unit,
    onCancelar: () -> Unit
) {
    var cantidad by remember { mutableStateOf(1) }
    val maxStock = producto.stock.coerceAtLeast(1)

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentPadding = PaddingValues(20.dp),
        glowColor = LiquidGlass.VerdeNeon,
        fondoSolido = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Agregar al carrito",
                style = MaterialTheme.typography.titleMedium,
                color = LiquidGlass.Texto
            )
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidGlass.TextoSuave,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Stock disponible: ${producto.stock}",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidGlass.TextoSuave
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (cantidad > 1) cantidad-- },
                    enabled = cantidad > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = LiquidGlass.VerdeNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = cantidad.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = LiquidGlass.Texto,
                    modifier = Modifier.width(44.dp)
                )
                IconButton(
                    onClick = { if (cantidad < maxStock) cantidad++ },
                    enabled = cantidad < maxStock
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = LiquidGlass.VerdeNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            GlassTextField(
                value = cantidad.toString(),
                onValueChange = {
                    cantidad = it.toIntOrNull()?.coerceIn(1, maxStock) ?: 1
                },
                label = "Cantidad",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                TextButton(onClick = onCancelar) {
                    Text("Cancelar", color = LiquidGlass.TextoSuave)
                }
                Button(
                    onClick = { onConfirmar(cantidad) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGlass.Esmeralda,
                        contentColor = Color(0xFF00271A)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Agregar")
                }
            }
        }
    }
}

@Composable
private fun ModalConfirmarVentaSheet(
    clientes: List<ClienteDto>,
    clienteId: Long?,
    tipoComprobante: String,
    onClienteChange: (Long?) -> Unit,
    onTipoComprobanteChange: (String) -> Unit,
    total: Double,
    unidades: Int,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    var menuCliente by remember { mutableStateOf(false) }
    val clienteSeleccionado = clientes.firstOrNull { it.id == clienteId }

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentPadding = PaddingValues(20.dp),
        glowColor = LiquidGlass.Azul,
        fondoSolido = true
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Confirmar Venta",
                style = MaterialTheme.typography.titleMedium,
                color = LiquidGlass.Texto
            )

            Column {
                Text(
                    text = "Tipo de comprobante",
                    style = MaterialTheme.typography.labelLarge,
                    color = LiquidGlass.TextoSuave
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("BOLETA", "FACTURA", "NOTA").forEach { tipo ->
                        GlassChip(
                            label = tipo,
                            selected = tipoComprobante == tipo,
                            onClick = { onTipoComprobanteChange(tipo) }
                        )
                    }
                }
            }

            Box {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { menuCliente = true },
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassIconTile(
                            icon = Icons.Default.Person,
                            tint = LiquidGlass.Azul,
                            container = LiquidGlass.Azul.copy(alpha = 0.12f),
                            size = 42
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Cliente",
                                style = MaterialTheme.typography.labelLarge,
                                color = LiquidGlass.TextoSuave
                            )
                            Text(
                                text = clienteSeleccionado?.let {
                                    listOfNotNull(it.nombre, it.apellido)
                                        .joinToString(" ")
                                        .ifBlank { "Cliente #${it.id}" }
                                } ?: "Sin cliente asignado",
                                style = MaterialTheme.typography.titleSmall,
                                color = LiquidGlass.Texto,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = LiquidGlass.TextoSuave
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuCliente,
                    onDismissRequest = { menuCliente = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Sin cliente asignado") },
                        onClick = {
                            onClienteChange(null)
                            menuCliente = false
                        }
                    )
                    clientes.forEach { cliente ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    listOfNotNull(cliente.nombre, cliente.apellido)
                                        .joinToString(" ")
                                        .ifBlank { "Cliente #${cliente.id}" }
                                )
                            },
                            onClick = {
                                onClienteChange(cliente.id)
                                menuCliente = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = LiquidGlass.Borde.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$unidades und. · Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "S/ ${formatearMonto(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = LiquidGlass.VerdeNeon
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                TextButton(onClick = onCancelar) {
                    Text("Cancelar", color = LiquidGlass.TextoSuave)
                }
                Button(
                    onClick = onConfirmar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LiquidGlass.Esmeralda,
                        contentColor = Color(0xFF00271A)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar venta")
                }
            }
        }
    }
}

@Composable
private fun ProductoVentaRow(
    producto: Producto,
    onAgregarClick: () -> Unit
) {
    val sinStock = producto.stock <= 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = if (sinStock) LiquidGlass.Peligro else LiquidGlass.VerdeNeon,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassIconTile(
                icon = Icons.Default.PointOfSale,
                tint = if (sinStock) LiquidGlass.Peligro else LiquidGlass.VerdeNeon,
                container = if (sinStock) {
                    LiquidGlass.Peligro.copy(alpha = 0.12f)
                } else {
                    LiquidGlass.VerdeNeon.copy(alpha = 0.12f)
                },
                size = 42
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = LiquidGlass.Texto,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = producto.precio.formateado(),
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.VerdeNeon
                    )
                    Text(
                        text = if (sinStock) "Agotado" else "Stock ${producto.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sinStock) LiquidGlass.Peligro else LiquidGlass.TextoSuave
                    )
                }
            }

            IconButton(
                onClick = onAgregarClick,
                enabled = !sinStock,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar ${producto.nombre}",
                    tint = if (sinStock) LiquidGlass.TextoSuave else LiquidGlass.VerdeNeon
                )
            }
        }
    }
}

private fun formatearMonto(valor: Double): String {
    val redondeado = round(valor * 100) / 100.0
    val entero = redondeado.toLong()
    val decimales = ((redondeado - entero) * 100).toInt().let { if (it < 0) -it else it }
    return "$entero.${decimales.toString().padStart(2, '0')}"
}
