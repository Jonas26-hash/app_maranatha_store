package com.maranathastore.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.data.tema.GestorTema
import com.maranathastore.app.data.tema.ModoTema
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.navigation.DestinoApp
import com.maranathastore.app.presentation.chat.MaranathaAIScreen
import com.maranathastore.app.presentation.clientes.ClientesScreen
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassNavigationBar
import com.maranathastore.app.presentation.components.GlassTopBar
import com.maranathastore.app.presentation.components.IconoMaranatha
import com.maranathastore.app.presentation.components.iconoDe
import com.maranathastore.app.presentation.inicio.InicioScreen
import com.maranathastore.app.presentation.login.LoginScreen
import com.maranathastore.app.presentation.login.LoginViewModel
import com.maranathastore.app.presentation.producto.ProductoScreen
import com.maranathastore.app.presentation.producto.ProductoViewModel
import com.maranathastore.app.presentation.reportes.ReportesScreen
import com.maranathastore.app.presentation.ventas.NuevaVentaScreen
import com.maranathastore.app.presentation.ventas.historial.HistorialVentasScreen
import com.maranathastore.app.theme.GlassBackground
import com.maranathastore.app.theme.LiquidGlass
import com.maranathastore.app.theme.MaranathaTheme
import com.maranathastore.app.util.AjustarBarrasSistema
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.clearAuthTokens
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val gestorSesion = koinInject<GestorSesion>()
    val gestorTema = koinInject<GestorTema>()
    val cliente = koinInject<HttpClient>()
    val scope = rememberCoroutineScope()

    val sesion by gestorSesion.sesion.collectAsStateWithLifecycle()
    val modoTema by gestorTema.modo.collectAsStateWithLifecycle()

    val sistemaOscuro = isSystemInDarkTheme()
    val esOscuro = when (modoTema) {
        ModoTema.OSCURO -> true
        ModoTema.CLARO -> false
        ModoTema.SISTEMA -> sistemaOscuro
    }

    AjustarBarrasSistema(oscuro = esOscuro)

    MaranathaTheme(darkTheme = esOscuro) {
        GlassBackground {
            val actual = sesion

            if (actual == null) {
                LoginScreen(
                    viewModel = koinViewModel<LoginViewModel>(),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ContenidoAutenticado(
                    sesion = actual,
                    modoTema = modoTema,
                    onCambiarTema = gestorTema::cambiar,
                    onCerrarSesion = {
                        scope.launch {
                            runCatching { cliente.clearAuthTokens() }
                            gestorSesion.limpiar()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ContenidoAutenticado(
    sesion: Sesion,
    modoTema: ModoTema,
    onCambiarTema: (ModoTema) -> Unit,
    onCerrarSesion: () -> Unit
) {
    var destinoActual by remember { mutableStateOf(DestinoApp.INICIO) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Black.copy(alpha = 0.62f),
        drawerContent = {
            DrawerLiquidGlass(
                sesion = sesion,
                destinoActual = destinoActual,
                modoTema = modoTema,
                onCambiarTema = onCambiarTema,
                onSeleccionar = { destino ->
                    destinoActual = destino
                    scope.launch { drawerState.close() }
                },
                onCerrarSesion = {
                    scope.launch { drawerState.close() }
                    onCerrarSesion()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                GlassTopBar(
                    titulo = destinoActual.titulo,
                    subtitulo = destinoActual.descripcion,
                    onMenu = { scope.launch { drawerState.open() } },
                    onMaranathaAi = { destinoActual = DestinoApp.MARANATHA_AI },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                GlassNavigationBar(
                    destinoActual = destinoActual,
                    onNavegar = { destinoActual = it },
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        ) { paddingValues ->
            Crossfade(
                targetState = destinoActual,
                animationSpec = tween(durationMillis = 300),
                label = "navCrossFade"
            ) { destino ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (destino) {
                        DestinoApp.INICIO -> InicioScreen(
                            sesion = sesion,
                            onNavegar = { destinoActual = it }
                        )

                        DestinoApp.PRODUCTOS -> ProductoScreen(
                            viewModel = koinViewModel<ProductoViewModel>(),
                            modifier = Modifier.fillMaxSize()
                        )

                        DestinoApp.CLIENTES -> ClientesScreen()
                        DestinoApp.NUEVA_VENTA -> NuevaVentaScreen()
                        DestinoApp.HISTORIAL -> HistorialVentasScreen()
                        DestinoApp.REPORTES -> ReportesScreen()
                        DestinoApp.MARANATHA_AI -> MaranathaAIScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerLiquidGlass(
    sesion: Sesion,
    destinoActual: DestinoApp,
    modoTema: ModoTema,
    onCambiarTema: (ModoTema) -> Unit,
    onSeleccionar: (DestinoApp) -> Unit,
    onCerrarSesion: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = Color.Transparent,
        drawerContentColor = LiquidGlass.Texto,
        modifier = Modifier
            .width(306.dp)
            .background(LiquidGlass.GradienteDrawer)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassIconTile(
                    icon = IconoMaranatha,
                    tint = LiquidGlass.VerdeNeon,
                    container = LiquidGlass.VerdeNeon.copy(alpha = 0.16f),
                    size = 52
                )

                Column {
                    Text(
                        text = "Maranatha Store",
                        style = MaterialTheme.typography.titleLarge,
                        color = LiquidGlass.Texto
                    )
                    Text(
                        text = "Librería cristiana",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.TextoSuave
                    )
                }
            }

            HorizontalDivider(color = LiquidGlass.Borde.copy(alpha = 0.22f))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DestinoApp.entries.forEach { destino ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = destino.titulo,
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        selected = destinoActual == destino,
                        onClick = { onSeleccionar(destino) },
                        icon = {
                            Icon(
                                imageVector = iconoDe(destino),
                                contentDescription = null,
                                modifier = Modifier.size(21.dp)
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = LiquidGlass.VerdeNeon.copy(alpha = 0.14f),
                            selectedTextColor = LiquidGlass.VerdeNeon,
                            selectedIconColor = LiquidGlass.VerdeNeon,
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = LiquidGlass.Texto,
                            unselectedIconColor = LiquidGlass.TextoSuave
                        )
                    )
                }

                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Apariencia",
                        style = MaterialTheme.typography.titleSmall,
                        color = LiquidGlass.Texto
                    )
                    Text(
                        text = "Elige el tema de la app",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.TextoSuave
                    )
                    SelectorTema(modoActual = modoTema, onCambiar = onCambiarTema)
                }
            }

            HorizontalDivider(color = LiquidGlass.Borde.copy(alpha = 0.22f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    LiquidGlass.VerdeNeon.copy(alpha = 0.28f),
                                    LiquidGlass.Azul.copy(alpha = 0.22f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sesion.usuario.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = LiquidGlass.VerdeNeon,
                        textAlign = TextAlign.Center
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sesion.usuario,
                        style = MaterialTheme.typography.titleSmall,
                        color = LiquidGlass.Texto
                    )
                    Text(
                        text = sesion.rol.etiqueta,
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidGlass.TextoSuave
                    )
                }
            }

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                selected = false,
                onClick = onCerrarSesion,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = LiquidGlass.Peligro,
                    unselectedIconColor = LiquidGlass.Peligro
                ),
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Maranatha Store · v0.1.0",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidGlass.TextoSuave.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 26.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun SelectorTema(
    modoActual: ModoTema,
    onCambiar: (ModoTema) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LiquidGlass.CampoSuave)
            .border(1.dp, LiquidGlass.Borde.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModoTema.entries.forEach { modo ->
            val seleccionado = modo == modoActual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (seleccionado) {
                            LiquidGlass.VerdeNeon.copy(alpha = 0.18f)
                        } else {
                            Color.Transparent
                        }
                    )
                    .clickable { onCambiar(modo) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modo.etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (seleccionado) {
                        LiquidGlass.VerdeNeon
                    } else {
                        LiquidGlass.TextoSuave
                    },
                    maxLines = 1
                )
            }
        }
    }
}
