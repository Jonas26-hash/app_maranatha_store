package com.maranathastore.app.presentation.ventas

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.data.remote.ComprobanteLocalDto
import com.maranathastore.app.data.remote.EvidenciaBetaDto

@Composable
actual fun recordarImpresionComprobante(venta: VentaDto, formato: String, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context, venta, formato, local, beta) {
        {
            try {
                if (formato == "TICKET" && android.os.Build.VERSION.SDK_INT >= 29) {
                    guardarTicket80(context, venta, local, beta)
                    Unit
                } else {
                val html = htmlComprobante(venta, formato, local, beta)
                val web = WebView(context)
                web.settings.javaScriptEnabled = false
                web.settings.allowFileAccess = false
                web.settings.allowContentAccess = false
                web.webViewClient = object : WebViewClient() {
                    private var iniciado = false
                    override fun onPageFinished(view: WebView, url: String?) {
                        if (iniciado) return
                        iniciado = true
                        try {
                            val media = if (formato == "TICKET") PrintAttributes.MediaSize("TICKET80", "Ticket 80 mm", 3150, 11693) else PrintAttributes.MediaSize.ISO_A4
                            val attrs = PrintAttributes.Builder().setMediaSize(media).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
                            val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            manager.print("Maranatha-${venta.serie}-${venta.numero}", view.createPrintDocumentAdapter("Comprobante"), attrs)
                        } catch (_: Exception) {
                            Toast.makeText(context, "No se pudo abrir la impresión", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Comprobante no disponible para imprimir", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
actual fun recordarCompartirComprobante(venta: VentaDto, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): (() -> Unit)? {
    val context = LocalContext.current
    if (android.os.Build.VERSION.SDK_INT < 29) return null
    return remember(context, venta, local, beta) {
        {
            try {
                val uri = guardarTicket80(context, venta, local, beta)
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = android.content.ClipData.newUri(context.contentResolver, "Comprobante PDF", uri)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Compartir comprobante"))
            } catch (_: Exception) { Toast.makeText(context, "No se pudo compartir el PDF", Toast.LENGTH_LONG).show() }
        }
    }
}
