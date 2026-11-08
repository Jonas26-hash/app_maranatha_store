package com.maranathastore.app.di

import com.maranathastore.app.data.auth.GestorSesion
import com.maranathastore.app.data.tema.GestorTema
import com.maranathastore.app.data.remote.AuthApi
import com.maranathastore.app.data.remote.AuthRepositorioRemoto
import com.maranathastore.app.data.remote.ClienteApi
import com.maranathastore.app.data.remote.MaranathaAiApi
import com.maranathastore.app.data.remote.ProductoApi
import com.maranathastore.app.data.remote.VentaApi
import com.maranathastore.app.data.remote.ProductoRepositorioRemoto
import com.maranathastore.app.data.remote.crearHttpClient
import com.maranathastore.app.domain.auth.AuthRepository
import com.maranathastore.app.domain.producto.ProductoRepository
import com.maranathastore.app.presentation.chat.MaranathaAIViewModel
import com.maranathastore.app.presentation.dashboard.DashboardViewModel
import com.maranathastore.app.presentation.inventario.InventarioViewModel
import com.maranathastore.app.presentation.clientes.ClientesViewModel
import com.maranathastore.app.presentation.login.LoginViewModel
import com.maranathastore.app.presentation.reportes.ReportesViewModel
import com.maranathastore.app.presentation.ventas.NuevaVentaViewModel
import com.maranathastore.app.presentation.ventas.historial.HistorialVentasViewModel
import com.maranathastore.app.presentation.ventas.SalesViewModel
import com.maranathastore.app.presentation.producto.ProductoViewModel
import io.ktor.client.HttpClient
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {

    single { GestorSesion(get()) }
    single { GestorTema(get()) }

    single<HttpClient> {
        crearHttpClient(
            motor = get(),
            leerToken = { get<GestorSesion>().token() },
            alExpirarSesion = { get<GestorSesion>().limpiar() }
        )
    }

    single { AuthApi(get()) }
    single<AuthRepository> { AuthRepositorioRemoto(get(), get()) }

    single { ProductoApi(get()) }
    single<ProductoRepository> { ProductoRepositorioRemoto(get()) }

    single { ClienteApi(get()) }

    single { VentaApi(get()) }
    single<com.maranathastore.app.domain.venta.VentaRepository> { com.maranathastore.app.domain.venta.VentaRepositorioRemoto(get()) }

    single { MaranathaAiApi(get()) }
}

val presentationModule = module {
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ProductoViewModel(get(), get()) }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { InventarioViewModel(get()) }
    viewModel { SalesViewModel() }
    viewModel { ClientesViewModel(get()) }
    viewModel { NuevaVentaViewModel(get(), get(), get()) }
    viewModel { HistorialVentasViewModel(get()) }
    viewModel { ReportesViewModel(get(), get()) }
    viewModel { MaranathaAIViewModel(get()) }
}

expect val platformModule: Module

fun initKoin(configuracionAdicional: KoinApplication.() -> Unit = {}) {
    startKoin {
        configuracionAdicional()
        modules(
            dataModule,
            presentationModule,
            platformModule
        )
    }
}
