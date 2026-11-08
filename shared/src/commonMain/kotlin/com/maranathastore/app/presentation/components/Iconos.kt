package com.maranathastore.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector
import com.maranathastore.app.navigation.DestinoApp

fun iconoDe(destino: DestinoApp): ImageVector = when (destino) {
    DestinoApp.INICIO -> Icons.Default.Home
    DestinoApp.PRODUCTOS -> Icons.Default.Inventory2
    DestinoApp.CLIENTES -> Icons.Default.People
    DestinoApp.NUEVA_VENTA -> Icons.Default.PointOfSale
    DestinoApp.HISTORIAL -> Icons.Default.History
    DestinoApp.REPORTES -> Icons.Default.QueryStats
    DestinoApp.MARANATHA_AI -> Icons.Default.SmartToy
}

val IconoMaranatha: ImageVector
    get() = Icons.AutoMirrored.Filled.MenuBook
