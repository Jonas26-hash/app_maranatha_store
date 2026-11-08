package com.maranathastore.app.presentation.clientes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.data.remote.ClienteDto
import com.maranathastore.app.data.remote.ClienteRequestDto
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassChip
import com.maranathastore.app.presentation.components.GlassEmptyState
import com.maranathastore.app.presentation.components.GlassErrorState
import com.maranathastore.app.presentation.components.GlassLoading
import com.maranathastore.app.presentation.components.GlassPill
import com.maranathastore.app.presentation.components.GlassSearchBar
import com.maranathastore.app.presentation.components.GlassTextField
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClientesScreen() {
    val vm = koinViewModel<ClientesViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GlassSearchBar(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        placeholder = "Buscar cliente por nombre o documento"
                    )
                }
                IconButton(
                    onClick = { vm.cargar() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar clientes",
                        tint = LiquidGlass.TextoSuave
                    )
                }
            }

            when (val s = state) {
                is ClientesUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    GlassLoading()
                }

                is ClientesUiState.Empty -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlassEmptyState(
                        titulo = "Aún no hay clientes",
                        message = "Registra tu primer cliente para agilizar tus ventas."
                    )
                }

                is ClientesUiState.Error -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlassErrorState(message = s.mensaje)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { vm.cargar() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidGlass.Esmeralda,
                            contentColor = Color(0xFF00271A)
                        )
                    ) {
                        Text("Reintentar")
                    }
                }

                is ClientesUiState.Success -> {
                    val filtrados = s.clientes.filter { cliente ->
                        val q = busqueda.trim().lowercase()
                        q.isBlank() ||
                            cliente.nombre?.lowercase()?.contains(q) == true ||
                            cliente.apellido?.lowercase()?.contains(q) == true ||
                            cliente.numeroDocumento?.contains(q) == true
                    }

                    if (filtrados.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            GlassEmptyState(
                                titulo = "Sin coincidencias",
                                message = "No encontramos clientes para “$busqueda”."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(
                                items = filtrados,
                                key = { indice, cliente -> cliente.id ?: indice }
                            ) { indice, cliente ->
                                Reveal(delayMillis = (indice.coerceAtMost(9)) * 40) {
                                    ClienteCard(cliente)
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { mostrarFormulario = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = LiquidGlass.Esmeralda,
            contentColor = Color(0xFF00271A),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar cliente"
            )
        }
    }

    if (mostrarFormulario) {
        NuevoClienteDialog(
            onCerrar = { mostrarFormulario = false },
            onGuardar = { dto ->
                vm.crear(dto)
                mostrarFormulario = false
            }
        )
    }
}

@Composable
private fun ClienteCard(cliente: ClienteDto) {
    val nombreCompleto = listOfNotNull(
        cliente.nombre?.takeIf { it.isNotBlank() },
        cliente.apellido?.takeIf { it.isNotBlank() }
    ).joinToString(" ").ifBlank { "Cliente sin nombre" }

    val documento = listOfNotNull(
        cliente.tipoDocumento?.takeIf { it.isNotBlank() },
        cliente.numeroDocumento?.takeIf { it.isNotBlank() }
    ).joinToString(" ")

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowColor = LiquidGlass.Azul,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LiquidGlass.Azul.copy(alpha = 0.30f),
                                LiquidGlass.VerdeNeon.copy(alpha = 0.18f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombreCompleto.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = LiquidGlass.Azul
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        color = LiquidGlass.Texto,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (cliente.activo == false) {
                        GlassPill(
                            text = "Inactivo",
                            color = LiquidGlass.TextoSuave,
                            container = LiquidGlass.CristalSuave
                        )
                    }
                }

                if (documento.isNotBlank()) {
                    GlassPill(
                        text = documento,
                        color = LiquidGlass.VerdeNeon,
                        container = LiquidGlass.VerdeNeon.copy(alpha = 0.10f)
                    )
                }

                cliente.email?.takeIf { it.isNotBlank() }?.let { correo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = LiquidGlass.TextoSuave,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = correo,
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                cliente.telefono?.takeIf { it.isNotBlank() }?.let { telefono ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = LiquidGlass.TextoSuave,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = telefono,
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave
                        )
                    }
                }

                cliente.direccion?.takeIf { it.isNotBlank() }?.let { direccion ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LiquidGlass.TextoSuave,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidGlass.TextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NuevoClienteDialog(
    onCerrar: () -> Unit,
    onGuardar: (ClienteRequestDto) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var tipoDoc by remember { mutableStateOf("DNI") }
    var numeroDoc by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }

    val puedeGuardar = nombre.isNotBlank() && numeroDoc.isNotBlank()

    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(16.dp),
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 620.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nuevo cliente",
                    style = MaterialTheme.typography.headlineSmall,
                    color = LiquidGlass.Texto
                )
                Text(
                    text = "Los datos se sincronizan con el backend.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave
                )

                GlassTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre *",
                    placeholder = "Ej. Ana"
                )

                GlassTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = "Apellido",
                    placeholder = "Ej. Pérez"
                )

                Text(
                    text = "Tipo de documento",
                    style = MaterialTheme.typography.labelLarge,
                    color = LiquidGlass.TextoSuave
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DNI", "RUC", "CE").forEach { tipo ->
                        GlassChip(
                            label = tipo,
                            selected = tipoDoc == tipo,
                            onClick = { tipoDoc = tipo }
                        )
                    }
                }

                GlassTextField(
                    value = numeroDoc,
                    onValueChange = { numeroDoc = it },
                    label = "Número de documento *",
                    placeholder = "Ej. 70123456",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                GlassTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "cliente@correo.com",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                GlassTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono",
                    placeholder = "Ej. 987654321",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                GlassTextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    label = "Dirección",
                    placeholder = "Calle, número, distrito",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                ) {
                    TextButton(onClick = onCerrar) {
                        Text("Cancelar", color = LiquidGlass.TextoSuave)
                    }
                    Button(
                        onClick = {
                            onGuardar(
                                ClienteRequestDto(
                                    nombre = nombre.trim(),
                                    apellido = apellido.trim().ifBlank { null },
                                    tipoDocumento = tipoDoc,
                                    numeroDocumento = numeroDoc.trim(),
                                    email = email.trim().ifBlank { null },
                                    telefono = telefono.trim().ifBlank { null },
                                    direccion = direccion.trim().ifBlank { null }
                                )
                            )
                        },
                        enabled = puedeGuardar,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LiquidGlass.Esmeralda,
                            contentColor = Color(0xFF00271A),
                            disabledContainerColor = LiquidGlass.CristalSuave,
                            disabledContentColor = LiquidGlass.TextoSuave
                        )
                    ) {
                        Text("Guardar cliente")
                    }
                }
            }
        }
    }
}
