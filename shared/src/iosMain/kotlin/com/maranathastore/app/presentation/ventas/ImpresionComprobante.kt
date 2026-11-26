package com.maranathastore.app.presentation.ventas
import androidx.compose.runtime.Composable
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.data.remote.ComprobanteLocalDto
import com.maranathastore.app.data.remote.EvidenciaBetaDto
@Composable actual fun recordarImpresionComprobante(venta: VentaDto, formato: String, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): (() -> Unit)? = null

@androidx.compose.runtime.Composable
actual fun recordarCompartirComprobante(venta: com.maranathastore.app.data.remote.VentaDto, local: com.maranathastore.app.data.remote.ComprobanteLocalDto?, beta: com.maranathastore.app.data.remote.EvidenciaBetaDto?): (() -> Unit)? = null
