package com.maranathastore.app.di

import com.maranathastore.app.data.remote.crearMotorHttp
import com.maranathastore.app.data.security.AlmacenSeguroKeychain
import com.maranathastore.app.data.tema.PreferenciasTema
import com.maranathastore.app.data.tema.PreferenciasTemaIos
import com.maranathastore.app.domain.security.AlmacenSeguro
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroKeychain() }
    single<PreferenciasTema> { PreferenciasTemaIos() }
    single<HttpClientEngine> { crearMotorHttp() }
}

fun initKoinIos() = initKoin()
