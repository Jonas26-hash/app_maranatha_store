package com.maranathastore.app.util

import androidx.compose.runtime.Composable

/**
 * Ajusta el contraste de los iconos de las barras del sistema según el tema
 * activo. En iOS no es necesario porque el sistema gestiona el contraste.
 */
@Composable
expect fun AjustarBarrasSistema(oscuro: Boolean)
