package com.maranathastore.app.presentation.components

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        enabled = enabled,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = LiquidGlass.CampoSuave,
            labelColor = LiquidGlass.TextoSuave,
            selectedContainerColor = LiquidGlass.VerdeNeon.copy(alpha = 0.18f),
            selectedLabelColor = LiquidGlass.VerdeNeon,
            disabledContainerColor = LiquidGlass.CampoHondo,
            disabledLabelColor = LiquidGlass.TextoSuave.copy(alpha = 0.4f)
        )
    )
}
