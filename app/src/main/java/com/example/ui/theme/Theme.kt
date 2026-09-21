package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MahiDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepSpaceBackground,
    primaryContainer = NeonPurple,
    onPrimaryContainer = TextPrimary,
    secondary = NeonPink,
    onSecondary = DeepSpaceBackground,
    secondaryContainer = DeepSpaceCard,
    onSecondaryContainer = TextSecondary,
    tertiary = NeonViolet,
    onTertiary = DeepSpaceBackground,
    background = DeepSpaceBackground,
    onBackground = TextPrimary,
    surface = DeepSpaceSurface,
    onSurface = TextPrimary,
    surfaceVariant = DeepSpaceCard,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = Color(0x20A855F7),
    error = NeonRed,
    onError = TextPrimary
)

@Composable
fun MahiAiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MahiDarkColorScheme,
        typography = Typography,
        content = content
    )
}
