package com.maranathastore.app.navigation

enum class DestinoApp(val titulo: String, val descripcion: String) {
    INICIO(
        titulo = "Inicio",
        descripcion = "Resumen de tu librería"
    ),
    PRODUCTOS(
        titulo = "Productos",
        descripcion = "Catálogo, precios y stock"
    ),
    NUEVA_VENTA(
        titulo = "Nueva venta",
        descripcion = "Boletas, facturas y notas de pedido"
    ),
    HISTORIAL(
        titulo = "Historial",
        descripcion = "Ventas emitidas y anulaciones"
    ),
    REPORTES(
        titulo = "Reportes",
        descripcion = "Indicadores y alertas locales"
    ),
    MI_YAPE("Mi Yape", "Receptor de notificaciones")
}
