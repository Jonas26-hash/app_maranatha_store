package com.maranathastore.app.domain.producto

data class ErroresProducto(
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: String? = null,
    val stock: String? = null,
    val cantidad: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(codigo, nombre, descripcion, precio, stock, cantidad).any { it != null }
}

sealed interface ResultadoValidacionProducto {
    data class Valido(val datos: DatosProducto) : ResultadoValidacionProducto
    data class Invalido(val errores: ErroresProducto) : ResultadoValidacionProducto
}

sealed interface ResultadoCantidad {
    data class Valida(val cantidad: Int) : ResultadoCantidad
    data class Invalida(val mensaje: String) : ResultadoCantidad
}

object ValidacionProducto {

    const val MAX_CODIGO = 30
    const val MAX_NOMBRE = 150
    const val MAX_DESCRIPCION = 500

    fun validar(
        codigo: String,
        nombre: String,
        descripcion: String,
        precioTexto: String,
        stockTexto: String,
        activo: Boolean = true,
        codigosExistentes: Set<String> = emptySet()
    ): ResultadoValidacionProducto {
        val codigoNormalizado = codigo.trim().uppercase()
        val nombreNormalizado = nombre.trim()
        val descripcionNormalizada = descripcion.trim().ifBlank { null }

        val errores = ErroresProducto(
            codigo = validarCodigo(codigoNormalizado, codigosExistentes),
            nombre = when {
                nombreNormalizado.isBlank() -> "El nombre del artículo es obligatorio."
                nombreNormalizado.length > MAX_NOMBRE ->
                    "El nombre no puede superar los $MAX_NOMBRE caracteres."
                else -> null
            },
            descripcion = if (descripcionNormalizada != null && descripcionNormalizada.length > MAX_DESCRIPCION) {
                "La descripción no puede superar los $MAX_DESCRIPCION caracteres."
            } else null,
            precio = validarPrecio(precioTexto),
            stock = validarStock(stockTexto)
        )

        if (errores.hayErrores) return ResultadoValidacionProducto.Invalido(errores)

        return ResultadoValidacionProducto.Valido(
            DatosProducto(
                codigo = codigoNormalizado,
                nombre = nombreNormalizado,
                descripcion = descripcionNormalizada,
                precio = Precio.desdeTexto(precioTexto)!!,
                stock = stockTexto.trim().toInt(),
                activo = activo
            )
        )
    }

    private fun validarCodigo(codigo: String, codigosExistentes: Set<String>): String? {
        if (codigo.isBlank()) return "El código del artículo es obligatorio."
        if (codigo.length > MAX_CODIGO) return "El código no puede superar los $MAX_CODIGO caracteres."
        if (!Regex("""^[A-Za-z0-9\-_./]+$""").matches(codigo)) {
            return "El código solo puede contener letras, números, guiones y puntos."
        }
        if (codigosExistentes.any { it.equals(codigo, ignoreCase = true) }) {
            return "El código '$codigo' ya está registrado en otro artículo."
        }
        return null
    }

    fun validarCantidad(texto: String): ResultadoCantidad {
        val valor = texto.trim()
        if (valor.isBlank()) {
            return ResultadoCantidad.Invalida("Ingresa la cantidad.")
        }
        if (!Regex("""^\d+$""").matches(valor)) {
            return ResultadoCantidad.Invalida("La cantidad debe ser mayor que 0.")
        }
        val cantidad = valor.toIntOrNull()
            ?: return ResultadoCantidad.Invalida("La cantidad es demasiado alta.")
        if (cantidad <= 0) {
            return ResultadoCantidad.Invalida("La cantidad debe ser mayor que 0.")
        }
        return ResultadoCantidad.Valida(cantidad)
    }

    private fun validarPrecio(texto: String): String? {
        val valor = texto.trim()
        if (valor.isBlank()) return "Ingresa el precio del artículo."
        val precio = Precio.desdeTexto(valor) ?: return "Ingresa un precio válido con hasta 2 decimales."
        if (!precio.esPositivo) return "El precio debe ser mayor que 0."
        return null
    }

    private fun validarStock(texto: String): String? {
        val valor = texto.trim()
        if (valor.isBlank()) return "Ingresa el stock inicial."
        if (valor.startsWith("-")) return "El stock no puede ser negativo."
        if (!Regex("""^\d+$""").matches(valor)) {
            return "Ingresa un stock válido (número entero)."
        }
        val stockNum = valor.toIntOrNull() ?: return "El stock es demasiado alto."
        if (stockNum < 0) return "El stock no puede ser negativo."
        return null
    }
}