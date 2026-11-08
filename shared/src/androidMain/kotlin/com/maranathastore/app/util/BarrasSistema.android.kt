package com.maranathastore.app.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun AjustarBarrasSistema(oscuro: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val ventana = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(ventana, view).apply {
                isAppearanceLightStatusBars = !oscuro
                isAppearanceLightNavigationBars = !oscuro
            }
        }
    }
}
