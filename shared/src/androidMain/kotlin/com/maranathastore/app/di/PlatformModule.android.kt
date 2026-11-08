package com.maranathastore.app.di

import com.maranathastore.app.data.remote.crearMotorHttp
import com.maranathastore.app.data.security.AlmacenSeguroAndroid
import com.maranathastore.app.data.tema.PreferenciasTema
import com.maranathastore.app.data.tema.PreferenciasTemaAndroid
import com.maranathastore.app.domain.security.AlmacenSeguro
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AlmacenSeguro> { AlmacenSeguroAndroid(androidContext()) }
    single<PreferenciasTema> { PreferenciasTemaAndroid(androidContext()) }
    single<HttpClientEngine> { crearMotorHttp() }
}
