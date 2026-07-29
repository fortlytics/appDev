package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF381E72),
    primaryContainer = ImmersivePurpleContainer,
    onPrimaryContainer = ImmersivePurpleLightContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF4A2532),
    secondaryContainer = Color(0xFF4A2532),
    onSecondaryContainer = ImmersivePinkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = ImmersiveTextPrimary,
    surface = DarkSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveBorder
)

private val LightColorScheme = darkColorScheme( // Force dark immersive aesthetic for cohesive UI
    primary = DarkPrimary,
    onPrimary = Color(0xFF381E72),
    primaryContainer = ImmersivePurpleContainer,
    onPrimaryContainer = ImmersivePurpleLightContainer,
    secondary = DarkSecondary,
    onSecondary = Color(0xFF4A2532),
    secondaryContainer = Color(0xFF4A2532),
    onSecondaryContainer = ImmersivePinkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = ImmersiveTextPrimary,
    surface = DarkSurface,
    onSurface = ImmersiveTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = ImmersiveTextSecondary,
    outline = ImmersiveBorder
)

@Composable
fun CoverageAnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand signal visual identity
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
