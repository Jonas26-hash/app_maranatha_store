package com.maranathastore.app.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta interna del sistema Liquid Glass. Los tokens de [LiquidGlass] leen
 * siempre la paleta activa, de modo que toda la app reacciona al cambio de
 * tema sin necesidad de tocar cada componente.
 */
private data class PaletaLiquidGlass(
    val fondo: Color,
    val fondoTeal: Color,
    val fondoProfundo: Color,
    val fondoInicio: Color,
    val fondoMedio: Color,
    val cristal: Color,
    val cristalSuave: Color,
    val cristalHondo: Color,
    val borde: Color,
    val bordeNeon: Color,
    val bordeAzul: Color,
    val bordeBrillo: Color,
    val bordeSombra: Color,
    val verdeNeon: Color,
    val esmeralda: Color,
    val azul: Color,
    val ambar: Color,
    val texto: Color,
    val textoSuave: Color,
    val peligro: Color,
    val superficieDialogo: Color,
    val campo: Color,
    val campoSuave: Color,
    val campoHondo: Color,
    val orbeVerde: Color,
    val orbeAzul: Color,
    val orbeEsmeralda: Color
)

private val PaletaOscura = PaletaLiquidGlass(
    fondo = Color(0xFF030708),
    fondoTeal = Color(0xFF06201F),
    fondoProfundo = Color(0xFF020506),
    fondoInicio = Color(0xFF041312),
    fondoMedio = Color(0xFF030A0C),
    cristal = Color(0x2BFFFFFF),
    cristalSuave = Color(0x14FFFFFF),
    cristalHondo = Color(0x08FFFFFF),
    borde = Color(0x40FFFFFF),
    bordeNeon = Color(0x663BFFB0),
    bordeAzul = Color(0x4D3AA8FF),
    bordeBrillo = Color(0x66FFFFFF),
    bordeSombra = Color(0x1AFFFFFF),
    verdeNeon = Color(0xFF3BFFB0),
    esmeralda = Color(0xFF19E68C),
    azul = Color(0xFF3AA8FF),
    ambar = Color(0xFFFFC46B),
    texto = Color(0xFFEAF7F3),
    textoSuave = Color(0xFF9FB8B1),
    peligro = Color(0xFFFF6B7A),
    superficieDialogo = Color(0xF20A1514),
    campo = Color(0x1FFFFFFF),
    campoSuave = Color(0x14FFFFFF),
    campoHondo = Color(0x0AFFFFFF),
    orbeVerde = Color(0xFF3BFFB0),
    orbeAzul = Color(0xFF3AA8FF),
    orbeEsmeralda = Color(0xFF19E68C)
)

/**
 * Tema claro con la misma identidad Liquid Glass: fondos menta muy claros,
 * cristales blancos translúcidos, acentos verdes profundos y bordes suaves.
 */
private val PaletaClara = PaletaLiquidGlass(
    fondo = Color(0xFFEFF7F3),
    fondoTeal = Color(0xFFDFF0E9),
    fondoProfundo = Color(0xFFE7F2ED),
    fondoInicio = Color(0xFFF7FCFA),
    fondoMedio = Color(0xFFEDF7F2),
    cristal = Color(0xB8FFFFFF),
    cristalSuave = Color(0x80FFFFFF),
    cristalHondo = Color(0x4DFFFFFF),
    borde = Color(0x33103A2E),
    bordeNeon = Color(0x6600A874),
    bordeAzul = Color(0x4D0A7BC2),
    bordeBrillo = Color(0xE6FFFFFF),
    bordeSombra = Color(0x33FFFFFF),
    verdeNeon = Color(0xFF00A874),
    esmeralda = Color(0xFF0FB97F),
    azul = Color(0xFF0A7BC2),
    ambar = Color(0xFFB87400),
    texto = Color(0xFF0B201B),
    textoSuave = Color(0xFF4A625B),
    peligro = Color(0xFFC22B3E),
    superficieDialogo = Color(0xF2FFFFFF),
    campo = Color(0xE6FFFFFF),
    campoSuave = Color(0xCCFFFFFF),
    campoHondo = Color(0x99FFFFFF),
    orbeVerde = Color(0xFF4CE0AE),
    orbeAzul = Color(0xFF7CC7FF),
    orbeEsmeralda = Color(0xFF6BE8BE)
)

/**
 * Tokens visuales del sistema Liquid Glass.
 * Los cristales usan alfa bajo para dejar ver el fondo con profundidad,
 * bordes luminosos de 1.dp y esquinas de 20.dp.
 */
object LiquidGlass {

    private val paleta = mutableStateOf(PaletaOscura)

    /**
     * Cambia la paleta activa. Se invoca desde [MaranathaTheme] para que todos
     * los tokens (incluidos los que se leen en dibujo o en valores por
     * defecto) se actualicen y recompongan automáticamente.
     */
    internal fun aplicarTema(oscuro: Boolean) {
        val nueva = if (oscuro) PaletaOscura else PaletaClara
        if (paleta.value !== nueva) paleta.value = nueva
    }

    val Fondo: Color get() = paleta.value.fondo
    val FondoTeal: Color get() = paleta.value.fondoTeal
    val FondoProfundo: Color get() = paleta.value.fondoProfundo

    val Cristal: Color get() = paleta.value.cristal
    val CristalSuave: Color get() = paleta.value.cristalSuave
    val CristalHondo: Color get() = paleta.value.cristalHondo

    val Borde: Color get() = paleta.value.borde
    val BordeNeon: Color get() = paleta.value.bordeNeon
    val BordeAzul: Color get() = paleta.value.bordeAzul

    val VerdeNeon: Color get() = paleta.value.verdeNeon
    val Esmeralda: Color get() = paleta.value.esmeralda
    val Azul: Color get() = paleta.value.azul
    val Ambar: Color get() = paleta.value.ambar

    val Texto: Color get() = paleta.value.texto
    val TextoSuave: Color get() = paleta.value.textoSuave
    val Peligro: Color get() = paleta.value.peligro
    val SuperficieDialogo: Color get() = paleta.value.superficieDialogo

    /** Fondos de campos de texto y chips. */
    val Campo: Color get() = paleta.value.campo
    val CampoSuave: Color get() = paleta.value.campoSuave
    val CampoHondo: Color get() = paleta.value.campoHondo

    /** Orbes de luz del fondo ambiental. */
    val OrbeVerde: Color get() = paleta.value.orbeVerde
    val OrbeAzul: Color get() = paleta.value.orbeAzul
    val OrbeEsmeralda: Color get() = paleta.value.orbeEsmeralda

    val GradienteFondo: Brush
        get() = Brush.verticalGradient(
            listOf(
                paleta.value.fondoInicio,
                paleta.value.fondoMedio,
                paleta.value.fondoProfundo
            )
        )

    val GradienteCristal: Brush
        get() = Brush.linearGradient(
            listOf(
                Cristal,
                CristalSuave,
                CristalHondo
            )
        )

    val GradienteNeon: Brush
        get() = Brush.linearGradient(
            listOf(VerdeNeon, Esmeralda, Azul)
        )

    val GradienteBorde: Brush
        get() = Brush.linearGradient(
            listOf(
                paleta.value.bordeBrillo,
                BordeNeon,
                paleta.value.bordeSombra,
                BordeAzul
            )
        )

    /** Barra de navegación flotante. */
    val GradienteBarra: Brush
        get() = Brush.verticalGradient(
            listOf(
                Cristal,
                CristalSuave,
                CristalHondo
            )
        )

    /** Fondo del menú lateral. */
    val GradienteDrawer: Brush
        get() = Brush.verticalGradient(
            listOf(FondoTeal, Fondo, FondoProfundo)
        )
}

/**
 * Fondo con profundidad: degradado vertical + orbes de luz difusa
 * (esmeralda y azul) que dan el efecto de vidrio sobre ambiente.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LiquidGlass.GradienteFondo)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ancho = size.width
            val alto = size.height
            val orbeVerde = LiquidGlass.OrbeVerde
            val orbeAzul = LiquidGlass.OrbeAzul
            val orbeEsmeralda = LiquidGlass.OrbeEsmeralda

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbeVerde.copy(alpha = 0.16f),
                        orbeEsmeralda.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(ancho * 0.12f, alto * 0.08f),
                    radius = ancho * 0.85f
                ),
                radius = ancho * 0.85f,
                center = Offset(ancho * 0.12f, alto * 0.08f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbeAzul.copy(alpha = 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(ancho * 0.95f, alto * 0.42f),
                    radius = ancho * 0.7f
                ),
                radius = ancho * 0.7f,
                center = Offset(ancho * 0.95f, alto * 0.42f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orbeEsmeralda.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(ancho * 0.25f, alto * 0.92f),
                    radius = ancho * 0.75f
                ),
                radius = ancho * 0.75f,
                center = Offset(ancho * 0.25f, alto * 0.92f)
            )
        }

        content()
    }
}
