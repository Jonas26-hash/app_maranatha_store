package com.maranathastore.app.di

import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.data.remote.AuthRepositorioRemoto
import com.maranathastore.app.data.remote.ProductoRepositorioRemoto
import com.maranathastore.app.domain.auth.AuthRepository
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.domain.security.AlmacenSeguro
import com.maranathastore.app.presentation.login.LoginViewModel
import com.maranathastore.app.presentation.producto.ProductoViewModel
import com.maranathastore.app.support.AlmacenSeguroFalso
import com.maranathastore.app.support.responderJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppModuleTest {

    @BeforeTest
    fun preparar() {
        stopKoin()
    }

    @AfterTest
    fun limpiar() {
        stopKoin()
    }

    @Test
    fun `el grafo de dependencias se resuelve con dobles de prueba`() {
        val almacen = AlmacenSeguroFalso()
        val motor = MockEngine { responderJson("{}") }

        val aplicacion = startKoin {
            modules(
                module {
                    single<AlmacenSeguro> { almacen }
                    single<HttpClientEngine> { motor }
                },
                dataModule,
                presentationModule
            )
        }
        val koin = aplicacion.koin

        try {
            val gestor = koin.get<GestorSesion>()
            val cliente = koin.get<HttpClient>()
            val repositorio = koin.get<AuthRepository>()
            val productoRepository = koin.get<ProductoRepository>()
            val viewModel = koin.get<LoginViewModel>()
            val viewModel2 = koin.get<LoginViewModel>()
            val productoViewModel = koin.get<ProductoViewModel>()

            assertIs<AuthRepositorioRemoto>(repositorio)
            assertIs<ProductoRepositorioRemoto>(productoRepository)
            assertSame(cliente, koin.get<HttpClient>())
            assertTrue(gestor.token() == null)
            assertIs<LoginViewModel>(viewModel)
            assertTrue(viewModel !== viewModel2)
            assertIs<ProductoViewModel>(productoViewModel)
        } finally {
            aplicacion.close()
        }
    }
}
