package com.maranathastore.app.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun crearMotorHttp(): HttpClientEngine = OkHttp.create()
