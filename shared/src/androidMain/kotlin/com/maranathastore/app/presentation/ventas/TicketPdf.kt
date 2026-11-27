package com.maranathastore.app.presentation.ventas

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.maranathastore.app.data.remote.*
import java.util.Locale

/** Saves a narrow PDF without relying on the printer's advertised media sizes. */
internal fun guardarTicket80(context: Context, venta: VentaDto, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): android.net.Uri {
    // Apply the same checks as the HTML printer before using archived evidence.
    htmlComprobante(venta, "TICKET", local, beta)
    val ancho = 227 // PDF points, approximately 80 mm
    val margen = 12
    val contenido = ancho - 2 * margen
    val bloques = mutableListOf<StaticLayout>()
    fun texto(s: String, size: Float = 10f, centro: Boolean = false, negrita: Boolean = false) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = size
            typeface = if (negrita) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        bloques.add(StaticLayout.Builder.obtain(s, 0, s.length, paint, contenido)
            .setAlignment(if (centro) Layout.Alignment.ALIGN_CENTER else Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(true).build())
    }
    fun monto(n: Double?) = n?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
    val id = "${venta.serie.orEmpty()}-${venta.numero.orEmpty()}"
    texto("MARANATHA STORE", 16f, true, true)
    if (beta != null) {
        val campos = beta.contenidoQr.split('|')
        texto("MARANATHA BETA - PRUEBA\nRUC de laboratorio: ${campos[0]}", 9f, true)
        texto("${venta.tipoComprobante} DE PRUEBA\n${beta.comprobante}", 12f, true, true)
        datosCompradorLocal(venta, beta != null)?.let { comprador ->
            texto("Comprador local (no fiscal): ${comprador.nombre}")
            comprador.documento?.let { texto("${comprador.etiquetaDocumento}: $it") }
        }
        texto("Receptor fiscal de prueba: ${beta.cliente ?: "Cliente de laboratorio"}\nRUC: ${campos[8]}\nFecha: ${beta.fecha}\nAceptado en Beta · Código 0")
        texto("PRODUCTOS", 10f, false, true)
        if (beta.detalles.isEmpty()) texto("Detalle no disponible")
        beta.detalles.forEach { texto("${it.descripcion}\n${it.cantidad} × S/ ${it.precioUnitario}     S/ ${it.importe}") }
        texto("Subtotal S/ ${beta.subtotal ?: "—"}\nIGV S/ ${beta.igv}")
        texto("TOTAL S/ ${beta.total}", 15f, true, true)
    } else {
        texto("${venta.tipoComprobante.orEmpty().replace('_',' ')} / REGISTRO INTERNO\n$id", 11f, true, true)
        datosCompradorLocal(venta, beta != null)?.let { comprador ->
            texto("Comprador local (no fiscal): ${comprador.nombre}")
            comprador.documento?.let { texto("${comprador.etiquetaDocumento}: $it") }
        }
        texto("Datos fiscales del emisor pendientes\nCliente: ${venta.clienteNombre ?: "Comprador no identificado"}\n${venta.clienteDocumento.orEmpty()}\nFecha: ${venta.fecha.orEmpty()}\nEstado: ${venta.estado.orEmpty()}")
        texto("PRODUCTOS", 10f, false, true)
        venta.detalles.orEmpty().forEach { texto("${it.productoNombre ?: it.productoId}\n${it.cantidad ?: 0} × S/ ${monto(it.precioUnitario)}     S/ ${monto(it.subtotal)}") }
        val datos = local?.takeIf { !it.validezFiscal && it.ambiente == "BORRADOR_LOCAL" && it.serie == venta.serie && it.numero == venta.numero && venta.estado == "EMITIDA" && venta.tipoComprobante in listOf("FACTURA", "BOLETA") && venta.total != null && kotlin.math.abs(it.total - venta.total) < 0.005 }
        if (datos != null) texto("Gravado S/ ${monto(datos.gravado)}\nExonerado S/ ${monto(datos.exonerado)}\nInafecto S/ ${monto(datos.inafecto)}\nIGV S/ ${monto(datos.igv)}") else texto("Desglose tributario no disponible")
        texto("TOTAL S/ ${monto(venta.total)}", 15f, true, true)
    }
    val qr = QRCodeWriter().encode(beta?.contenidoQr ?: "MARANATHA|REGISTRO INTERNO|$id", BarcodeFormat.QR_CODE, 1, 1)
    val qrTam = 108f
    val aviso = if (beta != null) "QR del XML firmado · Beta ${beta.comprobante}\nCOMPROBANTE DE PRUEBA\nSIN VALIDEZ FISCAL" else if (venta.serie in listOf("F9B1","B9B1","NP9B")) "QR de referencia interna\nREGISTRO DE PRUEBA · SIN VALIDEZ FISCAL" else "QR de referencia interna\nNO ACREDITA VALIDACIÓN SUNAT"
    val cuerpo = bloques.size
    texto(aviso, 9f, true, true)
    val alto = 2*margen + bloques.sumOf { it.height + 6 } + qrTam.toInt() + 12
    require(alto <= 14000) { "Ticket demasiado largo; use A4" }
    val pdf = PdfDocument()
    try {
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(ancho, alto, 1).create())
        val canvas = page.canvas
        var y = margen.toFloat()
        fun dibujar(layout: StaticLayout) { canvas.save(); canvas.translate(margen.toFloat(), y); layout.draw(canvas); canvas.restore(); y += layout.height + 6 }
        bloques.take(cuerpo).forEach { dibujar(it) }
        val x = (ancho-qrTam)/2f
        val paso = qrTam/qr.width
        val tinta = Paint().apply { color = Color.BLACK; isAntiAlias = false }
        for (j in 0 until qr.height) for (i in 0 until qr.width) if (qr[i,j]) canvas.drawRect(x+i*paso,y+j*paso,x+(i+1)*paso,y+(j+1)*paso,tinta)
        y += qrTam + 12
        dibujar(bloques.last())
        pdf.finishPage(page)
        val nombre = "Maranatha-${(beta?.comprobante ?: id).replace(Regex("[^A-Za-z0-9-]"), "_")}-Ticket-${System.currentTimeMillis()}.pdf"
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nombre)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Maranatha")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }) ?: error("No se pudo crear el PDF")
        try {
            resolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("No se pudo escribir el PDF")
            resolver.update(uri, ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }, null, null)
        } catch (e: Exception) { resolver.delete(uri, null, null); throw e }
        Toast.makeText(context, "PDF guardado en Descargas/Maranatha", Toast.LENGTH_LONG).show()
        return uri
    } finally { pdf.close() }
}
