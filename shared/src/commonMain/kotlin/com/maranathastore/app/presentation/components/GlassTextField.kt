package com.maranathastore.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.maranathastore.app.theme.LiquidGlass

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else 4,
    shape: Shape = RoundedCornerShape(18.dp)
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = LiquidGlass.VerdeNeon.copy(alpha = 0.85f)
                )
            }
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = shape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = LiquidGlass.Texto,
            unfocusedTextColor = LiquidGlass.Texto,
            disabledTextColor = LiquidGlass.TextoSuave,
            cursorColor = LiquidGlass.VerdeNeon,
            focusedContainerColor = LiquidGlass.Campo,
            unfocusedContainerColor = LiquidGlass.CampoSuave,
            disabledContainerColor = LiquidGlass.CampoHondo,
            focusedBorderColor = LiquidGlass.VerdeNeon.copy(alpha = 0.85f),
            unfocusedBorderColor = LiquidGlass.Borde.copy(alpha = 0.55f),
            disabledBorderColor = LiquidGlass.Borde.copy(alpha = 0.25f),
            focusedLabelColor = LiquidGlass.VerdeNeon,
            unfocusedLabelColor = LiquidGlass.TextoSuave,
            disabledLabelColor = LiquidGlass.TextoSuave.copy(alpha = 0.6f),
            focusedPlaceholderColor = LiquidGlass.TextoSuave.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = LiquidGlass.TextoSuave.copy(alpha = 0.7f),
            errorBorderColor = LiquidGlass.Peligro,
            errorLabelColor = LiquidGlass.Peligro,
            errorTextColor = LiquidGlass.Texto
        )
    )
}
