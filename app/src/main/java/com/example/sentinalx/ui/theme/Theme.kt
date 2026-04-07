package com.example.sentinalx.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DigitalRiskColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.Black,
    secondary = SurfaceDark,
    onSecondary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    error = DangerRed,
    onError = Color.White,
    outline = CardOutline
)

@Composable
fun DigitalRiskShieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DigitalRiskColorScheme,
        typography = Typography,
        content = content
    )
}
