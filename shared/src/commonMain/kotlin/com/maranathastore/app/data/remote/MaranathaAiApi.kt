package com.maranathastore.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val message: String
)

@Serializable
data class ChatResponseDto(
    val reply: String? = null
)

class MaranathaAiApi(
    private val client: HttpClient
) {
    suspend fun enviarMensaje(mensaje: String): ChatResponseDto =
        client.post("api/v1/chat") { setBody(ChatRequestDto(message = mensaje)) }.body()
}
