
package com.maranathastore.app.presentation.yape

import android.app.Notification
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.maranathastore.app.data.remote.ConfigApi
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import org.json.JSONObject
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal fun prefs(c: Context) =
    c.getSharedPreferences("mi_yape", Context.MODE_PRIVATE)

internal fun request(
    path: String,
    method: String,
    token: String,
    body: String? = null,
    device: Boolean = false
): String {

    val connection = URL(
        ConfigApi.BASE_URL.trimEnd('/') + "/ventas/yape/" + path
    ).openConnection() as HttpURLConnection

    try {
        connection.requestMethod = method
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        connection.setRequestProperty(
            if (device) "X-Yape-Token" else "Authorization",
            if (device) token else "Bearer $token"
        )

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.outputStream.use {
                it.write(body.toByteArray(Charsets.UTF_8))
            }
        }

        check(connection.responseCode in 200..299) {
            "HTTP ${connection.responseCode}"
        }

        return connection.inputStream
            .bufferedReader()
            .use { it.readText() }

    } finally {
        connection.disconnect()
    }
}

class YapeListener : NotificationListenerService() {

    private val executor =
        Executors.newSingleThreadScheduledExecutor()

    override fun onCreate() {
        super.onCreate()

        executor.scheduleWithFixedDelay(
            {
                runCatching {
                    enviar()
                }
            },
            0,
            15,
            TimeUnit.SECONDS
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()

        prefs(this).edit()
            .putString("estado", "Servicio de notificaciones conectado")
            .apply()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()

        prefs(this).edit()
            .putString("estado", "Servicio de notificaciones desconectado")
            .apply()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        // Solo procesar notificaciones de la aplicación oficial de Yape.
        if (sbn.packageName != "com.bcp.innovacxion.yapeapp") return

        val p = prefs(this)

        if (p.getString("token", null) == null) {
            p.edit()
                .putString("estado", "Dispositivo receptor no activado")
                .apply()
            return
        }

        p.edit()
            .putString("estado", "Notificación de Yape recibida")
            .apply()

        val extras = sbn.notification.extras

        val texto = listOf(
            Notification.EXTRA_TITLE,
            Notification.EXTRA_TEXT,
            Notification.EXTRA_BIG_TEXT
        ).mapNotNull {
            extras.getCharSequence(it)?.toString()
        }.distinct().joinToString(" ")

        // Reconocer diferentes formatos de pagos recibidos.
        val patronPago = Regex(
            """(?i)\b(te\s+envi[oó]\s+un\s+pago|te\s+yape[oó]|recibiste\s+(?:un\s+)?(?:pago|yape)|has\s+recibido\s+(?:un\s+)?(?:pago|yape))\b"""
        )

        if (!patronPago.containsMatchIn(texto)) {
            p.edit()
                .putString(
                    "estado",
                    "Notificación de Yape sin formato de pago reconocido"
                )
                .apply()
            return
        }

        // Extraer el importe, permitiendo un punto al final de la frase.
        val patronMonto = Regex(
            """S/\s*(\d+(?:[.,]\d{2})?)(?!\d|[.,]\d)""",
            RegexOption.IGNORE_CASE
        )

        val montos = patronMonto.findAll(texto)
            .map {
                it.groupValues[1].replace(',', '.')
            }
            .mapNotNull {
                it.toBigDecimalOrNull()?.stripTrailingZeros()
            }
            .distinct()
            .toList()

        if (montos.size != 1) {
            p.edit()
                .putString(
                    "estado",
                    "No se pudo identificar un importe único"
                )
                .apply()
            return
        }

        val monto = montos.single()

        if (monto.signum() <= 0) {
            p.edit()
                .putString("estado", "Importe de pago inválido")
                .apply()
            return
        }

        // Generar identificador para evitar eventos duplicados.
        val id = MessageDigest.getInstance("SHA-256")
            .digest(
                "${sbn.key}:${sbn.postTime}:${monto.toPlainString()}"
                    .toByteArray(Charsets.UTF_8)
            )
            .joinToString("") {
                "%02x".format(it.toInt() and 255)
            }

        // No se almacena el nombre del remitente ni el código de seguridad.
        val json = JSONObject()
            .put("id", id)
            .put("monto", monto.toPlainString())
            .put("fecha", sbn.postTime)
            .toString()

        executor.execute {

            if (p.getString("token", null) == null) {
                return@execute
            }

            val pendientes = p.all.keys.count {
                it.startsWith("evento_")
            }

            if (pendientes >= 500) {
                p.edit()
                    .putString(
                        "estado",
                        "Cola llena: revisar Yape manualmente"
                    )
                    .apply()

                return@execute
            }

            val guardado = p.edit()
                .putString("evento_$id", json)
                .putString(
                    "estado",
                    "Pago detectado: S/ ${monto.toPlainString()}"
                )
                .commit()

            if (!guardado) {
                p.edit()
                    .putString("estado", "Error al guardar el evento")
                    .apply()

                return@execute
            }

            runCatching {
                enviar()
            }
        }
    }

    private fun enviar() {

        val p = prefs(this)

        val token = p.getString("token", null) ?: return

        val eventos = p.all.filterKeys {
            it.startsWith("evento_")
        }

        for ((key, value) in eventos) {

            if (p.getString("token", null) != token) {
                return
            }

            try {

                request(
                    path = "eventos",
                    method = "POST",
                    token = token,
                    body = value as String,
                    device = true
                )

                p.edit()
                    .remove(key)
                    .putString("estado", "Último evento enviado correctamente")
                    .apply()

            } catch (e: Exception) {

                p.edit()
                    .putString(
                        "estado",
                        "Pendiente de envío: ${e.message}"
                    )
                    .apply()

                return
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
