package com.maranathastore.app.support

import com.maranathastore.app.domain.auth.AuthRepository
import com.maranathastore.app.domain.auth.Credenciales
import com.maranathastore.app.domain.auth.Sesion
import com.maranathastore.app.domain.producto.DatosProducto
import com.maranathastore.app.domain.producto.Precio
import com.maranathastore.app.domain.producto.Producto
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.domain.security.AlmacenSeguro
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred

/**
 * Almacén en memoria que permite observar cuántas veces la app guarda,
 * lee o borra la sesión. No representa al Keystore/Keychain reales.
 */
class AlmacenSeguroFalso(
    var sesionInicial: Sesion? = null
) : AlmacenSeguro {

    var sesionGuardada: Sesion? = null
        private set

    var guardados: Int = 0
        private set

    var borrados: Int = 0
        private set

    override fun guardarSesion(sesion: Sesion) {
        sesionGuardada = sesion
        sesionInicial = sesion
        guardados++
    }

    override fun leerSesion(): Sesion? = sesionInicial

    override fun borrarSesion() {
        sesionInicial = null
        sesionGuardada = null
        borrados++
    }
}

/**
 * Repositorio de autenticación controlable. `respuesta` permite simular un
 * backend lento y comprobar la protección contra doble envío.
 */
class AuthRepositoryFalso(
    var respuesta: CompletableDeferred<Sesion>? = null,
    var sesion: Sesion = Sesion("token", "admin", com.maranathastore.app.domain.auth.Rol.ADMIN),
    var fallo: Throwable? = null
) : AuthRepository {

    var llamadas: Int = 0
        private set

    var ultimasCredenciales: Credenciales? = null
        private set

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        llamadas++
        ultimasCredenciales = credenciales

        respuesta?.let { return it.await() }
        fallo?.let { throw it }
        return sesion
    }
}

fun MockRequestHandleScope.responderJson(
    cuerpo: String,
    estado: HttpStatusCode = HttpStatusCode.OK
): HttpResponseData = respond(
    content = cuerpo,
    status = estado,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
)

fun HttpRequestData.ruta(): String = url.encodedPath

fun HttpRequestData.autorizacion(): String? =
    headers[HttpHeaders.Authorization]

class ProductoRepositoryFalso(
    var productos: List<Producto> = emptyList(),
    var fallo: Throwable? = null
) : ProductoRepository {
    var listarLlamadas = 0
        private set
    var crearLlamadas = 0
        private set
    var actualizarLlamadas = 0
        private set
    var eliminarLlamadas = 0
        private set
    var reponerLlamadas = 0
        private set
    var descontarLlamadas = 0
        private set

    var ultimoCreado: DatosProducto? = null
        private set
    var ultimaActualizacion: Pair<Long, DatosProducto>? = null
        private set
    var ultimoEliminado: Long? = null
        private set
    var ultimaReposicion: Pair<Long, Int>? = null
        private set
    var ultimoDescuento: Pair<Long, Int>? = null
        private set

    private fun maybeThrow() {
        fallo?.let { throw it }
        fallo = null
    }

    override suspend fun listar(): List<Producto> {
        listarLlamadas++
        maybeThrow()
        return productos
    }

    override suspend fun obtener(id: Long): Producto {
        maybeThrow()
        return productos.first { it.id == id }
    }

    override suspend fun crear(datos: DatosProducto): Producto {
        crearLlamadas++
        maybeThrow()
        ultimoCreado = datos
        val producto = Producto(
            id = (productos.maxOfOrNull { it.id } ?: 0) + 1,
            codigo = datos.codigo,
            nombre = datos.nombre,
            descripcion = datos.descripcion,
            precio = datos.precio,
            stock = datos.stock,
            activo = datos.activo
        )
        productos = productos + producto
        return producto
    }

    override suspend fun actualizar(id: Long, datos: DatosProducto): Producto {
        actualizarLlamadas++
        maybeThrow()
        ultimaActualizacion = id to datos
        val actual = productos.first { it.id == id }
        val actualizado = actual.copy(
            codigo = datos.codigo,
            nombre = datos.nombre,
            descripcion = datos.descripcion,
            precio = datos.precio,
            activo = datos.activo,
            stock = datos.stock
        )
        productos = productos.map { if (it.id == id) actualizado else it }
        return actualizado
    }

    override suspend fun eliminar(id: Long) {
        eliminarLlamadas++
        maybeThrow()
        ultimoEliminado = id
        productos = productos.filter { it.id != id }
    }

    override suspend fun reponerStock(id: Long, cantidad: Int): Producto {
        reponerLlamadas++
        maybeThrow()
        ultimaReposicion = id to cantidad
        val actual = productos.first { it.id == id }
        val actualizado = actual.copy(stock = actual.stock + cantidad)
        productos = productos.map { if (it.id == id) actualizado else it }
        return actualizado
    }

    override suspend fun descontarStock(id: Long, cantidad: Int): Producto {
        descontarLlamadas++
        maybeThrow()
        ultimoDescuento = id to cantidad
        val actual = productos.first { it.id == id }
        val actualizado = actual.copy(stock = actual.stock - cantidad)
        productos = productos.map { if (it.id == id) actualizado else it }
        return actualizado
    }
}
