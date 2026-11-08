package com.maranathastore.app.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maranathastore.app.presentation.components.GlassButton
import com.maranathastore.app.presentation.components.GlassCard
import com.maranathastore.app.presentation.components.GlassIconButton
import com.maranathastore.app.presentation.components.GlassIconTile
import com.maranathastore.app.presentation.components.GlassTextField
import com.maranathastore.app.presentation.components.Reveal
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        estado = estado,
        onUsuarioChange = viewModel::onUsuarioChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onAlternarContrasena = viewModel::alternarContrasena,
        onIngresar = viewModel::ingresar,
        modifier = modifier
    )
}

@Composable
private fun LoginContent(
    estado: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onAlternarContrasena: () -> Unit,
    onIngresar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 460.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Reveal(delayMillis = 40) {
                GlassIconTile(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    size = 72,
                    tint = LiquidGlass.VerdeNeon,
                    container = LiquidGlass.VerdeNeon.copy(alpha = 0.14f)
                )
            }

            Spacer(Modifier.height(22.dp))

            Reveal(delayMillis = 120) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Maranatha Store",
                        style = MaterialTheme.typography.headlineLarge,
                        color = LiquidGlass.Texto,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Librería cristiana · Gestión de tienda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidGlass.TextoSuave,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Reveal(delayMillis = 200) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Iniciar sesión",
                                style = MaterialTheme.typography.titleLarge,
                                color = LiquidGlass.Texto
                            )
                            Text(
                                text = "Ingresa con las credenciales asignadas por tu administrador.",
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidGlass.TextoSuave
                            )
                        }

                        GlassTextField(
                            value = estado.usuario,
                            onValueChange = onUsuarioChange,
                            label = "Usuario",
                            leadingIcon = Icons.Default.Person,
                            singleLine = true,
                            enabled = !estado.cargando,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        GlassTextField(
                            value = estado.contrasena,
                            onValueChange = onContrasenaChange,
                            label = "Contraseña",
                            leadingIcon = Icons.Default.Lock,
                            singleLine = true,
                            enabled = !estado.cargando,
                            visualTransformation = if (estado.mostrarContrasena) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onIngresar() }),
                            trailingIcon = {
                                GlassIconButton(
                                    onClick = onAlternarContrasena,
                                    enabled = !estado.cargando,
                                    modifier = Modifier.size(38.dp),
                                    content = {
                                        Icon(
                                            imageVector = if (estado.mostrarContrasena) {
                                                Icons.Default.VisibilityOff
                                            } else {
                                                Icons.Default.Visibility
                                            },
                                            contentDescription = if (estado.mostrarContrasena) {
                                                "Ocultar contraseña"
                                            } else {
                                                "Mostrar contraseña"
                                            },
                                            tint = LiquidGlass.TextoSuave,
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                )
                            }
                        )

                        AnimatedVisibility(
                            visible = estado.error != null,
                            enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 3 },
                            exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { it / 3 }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(LiquidGlass.Peligro.copy(alpha = 0.14f))
                                    .border(
                                        1.dp,
                                        LiquidGlass.Peligro.copy(alpha = 0.35f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = LiquidGlass.Peligro,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = estado.error.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LiquidGlass.Peligro
                                )
                            }
                        }

                        GlassButton(
                            text = "Ingresar",
                            onClick = onIngresar,
                            enabled = estado.puedeEnviar,
                            loading = estado.cargando,
                            loadingText = "Ingresando…",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))

            Reveal(delayMillis = 300) {
                Text(
                    text = "El acceso y las operaciones se registran en el servidor de Maranatha Store.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidGlass.TextoSuave.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
