package com.currencyconverter.offline.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Blue = Color(0xFF2563EB)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)

private val DarkColors = darkColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    background = Slate900,
    onBackground = Color(0xFFF1F5F9),
    surface = Slate800,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = Slate800,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569)
)

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
