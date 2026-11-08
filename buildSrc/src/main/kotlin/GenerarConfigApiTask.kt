import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Genera la URL base del gateway en tiempo de compilación.
 *
 * Se usa una propiedad de Gradle en lugar de un archivo de configuración
 * versionado para que cada entorno (emulador, dispositivo físico, CI) apunte
 * al gateway correcto sin tocar código fuente ni guardar secretos.
 */
abstract class GenerarConfigApiTask : DefaultTask() {

    @get:Input
    abstract val urlBase: Property<String>

    @get:OutputDirectory
    abstract val directorioDestino: DirectoryProperty

    @TaskAction
    fun generar() {
        val url = urlBase.get().trim().trimEnd('/')
        val escapada = url.replace("\\", "\\\\").replace("\"", "\\\"")

        val archivo = directorioDestino.get().asFile
            .resolve("com/maranathastore/app/data/remote/ConfigApi.kt")

        archivo.parentFile.mkdirs()
        archivo.writeText(
            """
            package com.maranathastore.app.data.remote

            /**
             * URL raíz del API Gateway. Se resuelve desde la propiedad de
             * Gradle `maranathaApiBaseUrl` al compilar.
             */
            object ConfigApi {
                const val BASE_URL: String = "$escapada"
            }

            """.trimIndent()
        )
    }
}
