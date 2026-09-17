package com.example.chargenotifier.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

private val VibrantColorScheme = ColorScheme(
    primary = Color(0xFF00E676), // Vibrant Green
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFFB9F6CA),
    secondary = Color(0xFF00B0FF), // Vibrant Blue
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF01579B),
    onSecondaryContainer = Color(0xFF81D4FA),
    tertiary = Color(0xFFFFD600), // Vibrant Yellow
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFF57F17),
    onTertiaryContainer = Color(0xFFFFF176),
    background = Color.Black,
    onBackground = Color.White,
    surfaceContainerLow = Color(0xFF0D0D0D),
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF2D2D2D),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFFF5252),
    onError = Color.Black,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color.White,
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242)
)

@Composable
fun ChargeNotifierWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VibrantColorScheme,
        content = content
    )
}
