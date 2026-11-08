package com.maranathastore.app.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun crearMotorHttp(): HttpClientEngine = Darwin.create()
