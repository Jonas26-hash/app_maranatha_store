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
