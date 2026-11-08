package com.maranathastore.app.data.remote

import com.maranathastore.app.domain.producto.Precio
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.support.autorizacion
import com.maranathastore.app.support.responderJson
import com.maranathastore.app.support.ruta
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProductoRepositorioRemotoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `listar mapea productos y envia bearer`() = runTest {
        var autorizacion: String? = null
        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                autorizacion = solicitud.autorizacion()
                responderJson(
                    """[{"id":1,"nombre":"Biblia","descripcion":"RVR","precio":49.9,"stock":12,"activo":true,"version":3}]"""
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            val productos = repositorio.listar()
            assertEquals(1, productos.size)
            assertEquals("Biblia", productos[0].nombre)
            assertEquals("RVR", productos[0].descripcion)
            assertEquals(4990, productos[0].precio.centavos)
            assertEquals(12, productos[0].stock)
            assertEquals(true, productos[0].activo)
            assertEquals(3, productos[0].version)
            assertEquals("Bearer jwt-real", autorizacion)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `crear envia el contrato correcto y mapea respuesta`() = runTest {
        var metodo: HttpMethod? = null
        var cuerpo: ProductoRequestDto? = null
        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                metodo = solicitud.method
                cuerpo = json.decodeFromString<ProductoRequestDto>(
                    solicitud.body.toByteArray().decodeToString()
                )
                responderJson(
                    """{"id":7,"nombre":"Cuaderno","descripcion":null,"precio":5.5,"stock":20,"activo":true}""",
                    HttpStatusCode.Created
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            val producto = repositorio.crear(
                com.maranathastore.app.domain.producto.DatosProducto(
                    codigo = "ART-007",
                    nombre = "Cuaderno",
                    descripcion = null,
                    precio = Precio.desdeTexto("5.5")!!,
                    stock = 20,
                    activo = true
                )
            )
            assertEquals(HttpMethod.Post, metodo)
            assertEquals("ART-007", cuerpo?.codigo)
            assertEquals("Cuaderno", cuerpo?.nombre)
            assertEquals("[ART-007]", cuerpo?.descripcion)
            assertEquals(5.5, cuerpo?.precio)
            assertEquals(20, cuerpo?.stock)
            assertEquals(true, cuerpo?.activo)
            assertEquals(7, producto.id)
            assertEquals(550, producto.precio.centavos)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `actualizar envia put con stock incluido`() = runTest {
        var metodo: HttpMethod? = null
        var ruta: String? = null
        var cuerpo: ProductoRequestDto? = null
        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                metodo = solicitud.method
                ruta = solicitud.ruta()
                cuerpo = json.decodeFromString<ProductoRequestDto>(
                    solicitud.body.toByteArray().decodeToString()
                )
                responderJson(
                    """{"id":2,"nombre":"Lapicero","descripcion":"azul","precio":2.0,"stock":8,"activo":false}"""
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            val producto = repositorio.actualizar(
                2,
                com.maranathastore.app.domain.producto.DatosProducto(
                    codigo = "ART-002",
                    nombre = "Lapicero",
                    descripcion = "azul",
                    precio = Precio.desdeTexto("2")!!,
                    stock = 8,
                    activo = false
                )
            )
            assertEquals(HttpMethod.Put, metodo)
            assertEquals("/productos/2", ruta)
            assertEquals("ART-002", cuerpo?.codigo)
            assertEquals("[ART-002] azul", cuerpo?.descripcion)
            assertEquals(8, cuerpo?.stock)
            assertEquals(false, producto.activo)
            assertEquals(8, producto.stock)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `eliminar envia delete y acepta 204`() = runTest {
        var metodo: HttpMethod? = null
        var ruta: String? = null
        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                metodo = solicitud.method
                ruta = solicitud.ruta()
                respond(
                    content = "",
                    status = HttpStatusCode.NoContent
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            repositorio.eliminar(9)
            assertEquals(HttpMethod.Delete, metodo)
            assertEquals("/productos/9", ruta)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `reponer y descontar usan endpoints de stock`() = runTest {
        val rutas = mutableListOf<String>()
        val cantidades = mutableListOf<Int>()
        val cliente = crearHttpClient(
            motor = MockEngine { solicitud ->
                rutas += solicitud.ruta()
                val cuerpo = json.decodeFromString<StockRequestDto>(
                    solicitud.body.toByteArray().decodeToString()
                )
                cantidades += cuerpo.cantidad
                responderJson(
                    """{"id":4,"nombre":"Papel","descripcion":null,"precio":1.0,"stock":15,"activo":true}"""
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            repositorio.reponerStock(4, 5)
            repositorio.descontarStock(4, 3)
            assertEquals(listOf("/productos/4/stock/reponer", "/productos/4/stock/descontar"), rutas)
            assertEquals(listOf(5, 3), cantidades)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 409 se traduce a conflicto`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"timestamp":"x","status":409,"message":"Stock insuficiente"}""",
                    HttpStatusCode.Conflict
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            val fallo = kotlin.test.assertFailsWith<com.maranathastore.app.domain.ErrorDeApi> {
                repositorio.listar()
            }
            assertIs<com.maranathastore.app.domain.ErrorApi.Conflicto>(fallo.error)
            assertEquals("Stock insuficiente", fallo.mensaje)
        } finally {
            cliente.close()
        }
    }

    @Test
    fun `un 404 se traduce a no encontrado`() = runTest {
        val cliente = crearHttpClient(
            motor = MockEngine {
                responderJson(
                    """{"timestamp":"x","status":404,"message":"Producto no encontrado"}""",
                    HttpStatusCode.NotFound
                )
            },
            leerToken = { "jwt-real" }
        )
        try {
            val repositorio = ProductoRepositorioRemoto(ProductoApi(cliente))
            val fallo = kotlin.test.assertFailsWith<com.maranathastore.app.domain.ErrorDeApi> {
                repositorio.obtener(99)
            }
            assertIs<com.maranathastore.app.domain.ErrorApi.NoEncontrado>(fallo.error)
        } finally {
            cliente.close()
        }
    }

}