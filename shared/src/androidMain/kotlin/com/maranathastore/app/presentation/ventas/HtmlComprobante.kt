package com.maranathastore.app.presentation.ventas

import com.maranathastore.app.data.remote.VentaDto
import com.maranathastore.app.data.remote.ComprobanteLocalDto
import com.maranathastore.app.data.remote.EvidenciaBetaDto
import java.util.Locale
import java.math.BigDecimal
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

private fun esc(v: Any?): String = (v?.toString() ?: "").replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;")
private fun dinero(v: Double?): String = v?.let { String.format(Locale.US, "%.2f", it) } ?: "—"
private fun qrSvg(contenido: String): String {
    val matrix = QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, 180, 180)
    val path = buildString {
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) if (matrix[x,y]) append("M${x},${y}h1v1h-1z")
    }
    return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${matrix.width} ${matrix.height}" shape-rendering="crispEdges"><rect width="100%" height="100%" fill="white"/><path d="$path" fill="black"/></svg>"""
}
internal fun htmlComprobante(venta: VentaDto, formato: String, local: ComprobanteLocalDto?, beta: EvidenciaBetaDto?): String {
    val ticket = formato == "TICKET"
    val id = "${venta.serie.orEmpty()}-${venta.numero.orEmpty()}"
    val esBeta = beta != null
    val integrada = esBeta && venta.serie in listOf("B001", "F001")
    val campos = beta?.contenidoQr?.split('|')
    if (beta != null) {
        require(beta.ambiente == "BETA" && !beta.validezFiscal && beta.estadoSunat == "ACEPTADO_BETA" && beta.codigo == "0" && (beta.comprobante == id || (venta.serie in listOf("B001", "F001") && beta.comprobante.startsWith(if (venta.tipoComprobante == "BOLETA") "B9B1-" else "F9B1-"))))
        require(campos != null && campos.size == 10 && campos[2]+"-"+campos[3] == beta.comprobante && campos[1] == (if (venta.tipoComprobante == "BOLETA") "03" else "01") && campos[6] == beta.fecha && campos[9].isNotBlank())
        require(BigDecimal(campos[4]).compareTo(BigDecimal(beta.igv)) == 0 && BigDecimal(campos[5]).compareTo(BigDecimal(beta.total)) == 0)
    }
    val datos = local?.takeIf { !esBeta && !it.validezFiscal && it.ambiente == "BORRADOR_LOCAL" && it.serie == venta.serie && it.numero == venta.numero && venta.estado == "EMITIDA" && venta.tipoComprobante in listOf("FACTURA", "BOLETA") && venta.total != null && kotlin.math.abs(it.total - venta.total) < 0.005 }
    val tipo = if (esBeta) "${venta.tipoComprobante} DE PRUEBA" else esc(venta.tipoComprobante?.replace('_',' ') ?: "COMPROBANTE") + " / REGISTRO INTERNO"
    val estado = if (esBeta) "Aceptado en Beta · Código 0" else esc(venta.estado ?: "Sin estado")
    val empresa = if (esBeta && !integrada) "MARANATHA BETA - PRUEBA" else "MARANATHA STORE"
    val emisor = if (esBeta) "RUC de laboratorio: ${esc(campos!![0])}" else "Datos fiscales del emisor pendientes"
    val cliente = if (esBeta && !integrada) "${esc(beta?.cliente ?: "Cliente de laboratorio")} · RUC ${esc(campos!![8])}" else esc(venta.clienteNombre ?: "Comprador no identificado") + "<br>" + esc(venta.clienteDocumento)
    val compradorLocal = venta.compradorLocalNombre?.let { "<b>Comprador local (no fiscal):</b> ${esc(it)}<br>${esc(venta.compradorLocalDocumento)}<br>" } ?: ""
    val fecha = esc(beta?.fecha ?: venta.fecha?.replace('T',' ')?.take(19))
    val filas = venta.detalles.orEmpty().joinToString("") { "<tr><td>${esc(it.productoNombre ?: it.productoId)}</td><td class='num'>${esc(it.cantidad)}</td><td class='num'>${dinero(it.precioUnitario)}</td><td class='num'>${dinero(it.subtotal)}</td></tr>" }
    val filasBeta = beta?.detalles.orEmpty().joinToString("") { "<tr><td>${esc(it.descripcion)}</td><td class='num'>${esc(it.cantidad)}</td><td class='num'>${esc(it.precioUnitario)}</td><td class='num'>${esc(it.importe)}</td></tr>" }
    val detalle = if (esBeta && !integrada && beta!!.detalles.isEmpty()) "<p class='nota'>Detalle no disponible</p>" else "<table><thead><tr><th>Producto</th><th class='num'>Cant.</th><th class='num'>Precio</th><th class='num'>Importe</th></tr></thead><tbody>${if(esBeta && !integrada) filasBeta else filas}</tbody></table>"
    val impuestos = if (beta != null) "<div>Subtotal S/ ${esc(beta.subtotal ?: "—")}</div><div>IGV S/ ${esc(beta.igv)}</div>" else if (datos != null) "<div>Gravado S/ ${dinero(datos.gravado)}</div><div>Exonerado S/ ${dinero(datos.exonerado)}</div><div>Inafecto S/ ${dinero(datos.inafecto)}</div><div>IGV S/ ${dinero(datos.igv)}</div>" else "<div class='nota'>Desglose tributario no disponible</div>"
    val total = if (beta != null) esc(beta.total) else dinero(venta.total)
    val qr = qrSvg(beta?.contenidoQr ?: "MARANATHA|REGISTRO INTERNO|$id")
    val etiquetaQr = if (esBeta) "QR del XML firmado · Beta ${beta?.comprobante}" else "QR de referencia interna"
    val aviso = if (esBeta) "COMPROBANTE DE PRUEBA — SIN VALIDEZ FISCAL" else if (venta.serie in listOf("F9B1","B9B1","NP9B")) "REGISTRO DE PRUEBA — SIN VALIDEZ FISCAL" else "REGISTRO INTERNO — NO ACREDITA VALIDACIÓN SUNAT"
    return """<!doctype html><html><head><meta charset="utf-8"><style>
    @page{size:${if(ticket) "80mm 297mm" else "A4"};margin:0}*{box-sizing:border-box}
    body{margin:0;background:#fff;color:#111;font-family:Arial,sans-serif;font-size:${if(ticket) "11px" else "13px"};padding:${if(ticket) "4mm" else "14mm"};overflow-wrap:anywhere}
    .marca{text-align:center;font-size:${if(ticket) "18px" else "25px"};font-weight:800;letter-spacing:1px}.empresa{text-align:center;line-height:1.5}.doc{text-align:center;border:1px solid #222;padding:9px;margin:18px 0;line-height:1.6}.aviso{text-align:center;border:2px solid #222;padding:7px;font-weight:bold;font-size:11px}.meta{margin:14px 0;line-height:1.6}.qr{text-align:center;break-inside:avoid;margin-top:16px}.qr svg{display:block;width:${if(ticket) "35mm" else "38mm"};height:auto;margin:auto}.qr small{display:block;margin-top:4px}table{width:100%;border-collapse:collapse;table-layout:fixed;margin:12px 0}th{background:#eef0f2;font-weight:bold}th,td{border-bottom:1px solid #bbb;text-align:left;padding:6px 2px}th:first-child{width:43%}.num{text-align:right}tr{break-inside:avoid}.totales{text-align:right;line-height:1.7;border-top:1px solid #222;padding-top:8px;break-inside:avoid}.total{font-size:${if(ticket) "17px" else "24px"};font-weight:bold}.nota{font-size:10px}.footer{text-align:center;margin-top:15px;border-top:1px dashed #777;padding-top:9px;font-size:10px}
    </style></head><body><div class="marca">MARANATHA STORE</div><div class="empresa">$empresa<br>$emisor</div><div class="doc"><b>$tipo</b><br>${esc(id)}</div><div class="aviso">$aviso</div><div class="meta">$compradorLocal<b>${if(esBeta && !integrada) "Receptor fiscal de prueba:" else "Cliente:"}</b> $cliente<br><b>Fecha:</b> $fecha<br><b>Estado:</b> $estado</div>$detalle<div class="totales">$impuestos<div class="total">TOTAL S/ $total</div></div><div class="qr">$qr<small>$etiquetaQr</small></div><div class="footer">$aviso</div></body></html>"""
}
