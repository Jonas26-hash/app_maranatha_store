package com.maranathastore.app.presentation.ventas

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.maranathastore.app.data.remote.VentaDto
import java.util.Locale
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private fun esc(v: Any?): String = (v?.toString() ?: "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
private fun dinero(v: Double?): String = v?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
private fun qrInterno(venta: VentaDto): String {
    val matrix = QRCodeWriter().encode("MARANATHA|REGISTRO INTERNO|${venta.serie}-${venta.numero}", BarcodeFormat.QR_CODE, 120, 120)
    val path = buildString {
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) if (matrix[x,y]) append("M${x},${y}h1v1h-1z")
    }
    return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${matrix.width} ${matrix.height}" shape-rendering="crispEdges"><rect width="100%" height="100%" fill="white"/><path d="$path" fill="black"/></svg>"""
}

@Composable
actual fun recordarImpresionComprobante(venta: VentaDto, formato: String): (() -> Unit)? {
    val context = LocalContext.current
    return remember(context, venta, formato) {
        {
            val ticket = formato == "TICKET"
            val tipo = esc(venta.tipoComprobante?.replace('_', ' '))
            val serie = esc("${venta.serie.orEmpty()}-${venta.numero.orEmpty()}")
            val fecha = esc(venta.fecha?.replace('T', ' ')?.take(19))
            val qr = qrInterno(venta)
            val filas = venta.detalles.orEmpty().joinToString("") { "<tr><td>${esc(it.productoNombre ?: it.productoId)}</td><td class='num'>${esc(it.cantidad)}</td><td class='num'>${dinero(it.precioUnitario)}</td><td class='num'>${dinero(it.subtotal)}</td></tr>" }
            val empresa = "<div class='empresa'><b>MARANATHA STORE</b><br>Librería escolar<br>Tacna, Perú<br><small>Datos fiscales del emisor pendientes</small></div>"
            val caja = "<div class='doc'><b>$tipo</b><br>$serie</div>"
            val qrBox = "<div class='qr'>$qr<small>Referencia interna</small></div>"
            val cabecera = if (ticket) "$empresa$caja" else "<header>$empresa$qrBox$caja</header>"
            val html = """<!doctype html><html><head><meta charset="utf-8"><style>
            @page { size: ${if(ticket) "80mm 297mm" else "A4"}; margin: 0; }
            *{box-sizing:border-box} body{margin:0;color:#111;background:white;font-family:Arial,sans-serif;font-size:${if(ticket) "10px" else "13px"};padding:${if(ticket) "4mm" else "12mm"}}
            header{display:table;width:100%;margin-bottom:18px} header>div{display:table-cell;width:33.33%;vertical-align:middle;text-align:center} .empresa{text-align:center;line-height:1.5}.empresa b{font-size:${if(ticket) "15px" else "20px"}} .doc{border:1px solid #222;text-align:center;padding:10px;line-height:1.6;margin:8px 0}.qr{text-align:center}.qr svg{width:${if(ticket) "29mm" else "25mm"};height:auto;display:block;margin:auto}.qr small{display:block;font-size:9px}.meta{border-top:1px solid #222;border-bottom:1px solid #222;padding:8px 0;line-height:1.6;overflow-wrap:anywhere}table{width:100%;border-collapse:collapse;table-layout:fixed;margin:10px 0}th,td{padding:6px 2px;border-bottom:1px solid #aaa;overflow-wrap:anywhere;text-align:left}th:first-child{width:40%}.num{text-align:right}.total{text-align:right;font-weight:bold;font-size:${if(ticket) "13px" else "18px"};border-top:1px solid #222;padding:10px 0}.footer{font-size:10px;border-top:1px dashed #777;padding-top:8px;text-align:center;line-height:1.5}tr{break-inside:avoid}
            </style></head><body>$cabecera<div class="meta"><b>Cliente:</b> ${esc(venta.clienteNombre ?: "Público general")}<br>${esc(venta.clienteDocumento)}<br><b>Fecha:</b> $fecha<br><b>Estado interno:</b> ${esc(venta.estado)}</div><table><thead><tr><th>Producto</th><th class="num">Cant.</th><th class="num">Precio</th><th class="num">Importe</th></tr></thead><tbody>$filas</tbody></table><div class="total">TOTAL S/ ${dinero(venta.total)}</div><div class="footer">Gracias por su compra<br>Registro interno. No acredita validación SUNAT.<br>Desglose tributario pendiente de información fiscal del backend.</div>${if(ticket) qrBox else ""}</body></html>"""
            val web = WebView(context)
            web.webViewClient = object : WebViewClient() {
                private var iniciado = false
                override fun onPageFinished(view: WebView, url: String?) {
                    if (iniciado) return
                    iniciado = true
                    val media = if (ticket) PrintAttributes.MediaSize("TICKET80", "Ticket 80 mm", 3150, 11693) else PrintAttributes.MediaSize.ISO_A4
                    val attrs = PrintAttributes.Builder().setMediaSize(media).setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
                    val manager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                    manager.print("Maranatha-${venta.serie}-${venta.numero}", view.createPrintDocumentAdapter("Comprobante"), attrs)
                }
            }
            web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        }
    }
}
