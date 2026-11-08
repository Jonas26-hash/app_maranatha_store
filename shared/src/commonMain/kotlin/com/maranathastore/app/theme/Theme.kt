package com.maranathastore.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorOnPrimary = Color(0xFF00271A)
private val ColorPrimaryContainer = Color(0xFF0B3A2E)
private val ColorOnPrimaryContainer = Color(0xFFB9FFE0)
private val ColorOnSecondary = Color(0xFF001E33)
private val ColorSecondaryContainer = Color(0xFF0A2C40)
private val ColorOnSecondaryContainer = Color(0xFFBFE3FF)
private val ColorOnTertiary = Color(0xFF2A1A00)
private val ColorTertiaryContainer = Color(0xFF3B2A0A)
private val ColorOnTertiaryContainer = Color(0xFFFFE1AE)
private val ColorOnError = Color(0xFF3A0910)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeNeon,
    onPrimary = ColorOnPrimary,
    primaryContainer = ColorPrimaryContainer,
    onPrimaryContainer = ColorOnPrimaryContainer,
    secondary = AzulElectrico,
    onSecondary = ColorOnSecondary,
    secondaryContainer = ColorSecondaryContainer,
    onSecondaryContainer = ColorOnSecondaryContainer,
    tertiary = AmbarNeon,
    onTertiary = ColorOnTertiary,
    tertiaryContainer = ColorTertiaryContainer,
    onTertiaryContainer = ColorOnTertiaryContainer,
    background = NegroProfundo,
    onBackground = TextoClaro,
    surface = TealSuperficie,
    onSurface = TextoClaro,
    surfaceVariant = TealElevado,
    onSurfaceVariant = TextoSecundario,
    surfaceContainer = TealSuperficie,
    surfaceContainerHigh = TealElevado,
    outline = ContornoNeon,
    outlineVariant = ContornoSuaveNeon,
    error = Peligro,
    onError = ColorOnError,
    errorContainer = PeligroContenedor,
    onErrorContainer = PeligroTexto,
    scrim = androidx.compose.ui.graphics.Color.Black
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeProfundo,
    onPrimary = Papel,
    primaryContainer = MentaClaro,
    onPrimaryContainer = VerdeProfundoOscuro,
    secondary = AzulElectrico,
    onSecondary = Papel,
    secondaryContainer = Color(0xFFD7ECFF),
    onSecondaryContainer = Color(0xFF00243D),
    tertiary = AmbarNeon,
    onTertiary = Tinta,
    background = Crema,
    onBackground = Tinta,
    surface = Papel,
    onSurface = Tinta,
    surfaceVariant = ColorSuperficieVariante,
    onSurfaceVariant = ColorTextoVariante,
    outline = ColorContorno,
    outlineVariant = ColorContornoSuave,
    error = Peligro,
    onError = Papel,
    errorContainer = PeligroContenedor,
    onErrorContainer = PeligroTexto,
    scrim = androidx.compose.ui.graphics.Color.Black
)

private val Tipografia = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        ),
        displayMedium = base.displayMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp
        ),
        displaySmall = base.displaySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        ),
        headlineLarge = base.headlineLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
        ),
        headlineMedium = base.headlineMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        ),
        headlineSmall = base.headlineSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold
        ),
        titleLarge = base.titleLarge.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = base.titleMedium.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold
        ),
        labelLarge = base.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp
        ),
        labelMedium = base.labelMedium.copy(letterSpacing = 0.2.sp)
    )
}

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun MaranathaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    // Sincroniza la paleta Liquid Glass con el esquema de Material. Los tokens
    // son estado de Compose, por lo que el cambio se propaga sin reiniciar.
    SideEffect {
        LiquidGlass.aplicarTema(darkTheme)
    }

    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}
