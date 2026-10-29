package com.hourstracker.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Navy = Color(0xFF1E3A5F)
val TotalsBackground = Color(0xFF34495E)
val TotalsBlue = Color(0xFF6CB4EE)
val TotalsGreen = Color(0xFF4CD98A)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF1A73E8),
    background = Color(0xFFF4F4F4),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFF1F1F1),
    onSurfaceVariant = Color(0xFF5F6368),
    outline = Color(0xFF8A8A8A),
    outlineVariant = Color(0xFF8A8A8A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4F8),
    onPrimary = Color(0xFF062E6F),
    primaryContainer = Color(0xFF1F2E48),
    onPrimaryContainer = Color(0xFF8AB4F8),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF6A6A6A),
    outlineVariant = Color(0xFF5A5A5A)
)

object AppColors {
    val payGreen: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF5BD68F) else Color(0xFF2EA05F)

    val muted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
fun HoursTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
