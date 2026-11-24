package com.maranathastore.app.presentation.ventas.historial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlin.math.floor

@Composable
actual fun QrEvidenciaBeta(contenido: String) {
    val matrix = remember(contenido) { QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, 1, 1) }
    Canvas(Modifier.size(210.dp)) {
        drawRect(Color.White)
        val step = floor(minOf(size.width / matrix.width, size.height / matrix.height)).coerceAtLeast(1f)
        val ox = floor((size.width - step * matrix.width) / 2f)
        val oy = floor((size.height - step * matrix.height) / 2f)
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) if (matrix[x,y])
            drawRect(Color.Black, Offset(ox + x * step, oy + y * step), Size(step,step))
    }
}
