package com.maranathastore.app.presentation.ventas

import androidx.lifecycle.ViewModel
import com.maranathastore.app.domain.producto.Producto

class SalesViewModel : ViewModel() {
    val carrito = mutableListOf<Producto>()
}