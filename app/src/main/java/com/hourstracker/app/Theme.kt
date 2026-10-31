package com.hourstracker.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours of one theme. Following Apple's Dark Mode guidance, the dark themes are not
 * simple inversions: surfaces get lighter the more "elevated" they are, text keeps
 * a contrast of at least 4.5:1, and accent colours are lightened for dark backgrounds.
 */
data class AppPalette(
    val dark: Boolean,
    val scheme: ColorScheme,
    val header: Color,
    val totalsBg: Color,
    val totalsBlue: Color,
    val totalsGreen: Color,
    val payGreen: Color,
    val negative: Color
)

private val ClassicLight = AppPalette(
    dark = false,
    scheme = lightColorScheme(
        primary = Color(0xFF1967D2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3EDFC),
        onPrimaryContainer = Color(0xFF124FA3),
        secondary = Color(0xFF1967D2),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE3EDFC),
        onSecondaryContainer = Color(0xFF124FA3),
        background = Color(0xFFF2F2F7),
        onBackground = Color(0xFF1C1C1E),
        surface = Color.White,
        onSurface = Color(0xFF1C1C1E),
        surfaceVariant = Color(0xFFEFEFF4),
        onSurfaceVariant = Color(0xFF5C5C63),
        outline = Color(0xFF8A8A90),
        outlineVariant = Color(0xFFC9C9CF),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF7F7FA),
        surfaceContainer = Color(0xFFF2F2F7),
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color(0xFFEFEFF4),
        error = Color(0xFFC62828)
    ),
    header = Color(0xFF1E3A5F),
    totalsBg = Color(0xFF2F4560),
    totalsBlue = Color(0xFF8CC8F5),
    totalsGreen = Color(0xFF5FE0A0),
    payGreen = Color(0xFF1B7F46),
    negative = Color(0xFFFFB4A9)
)

private val MintLight = AppPalette(
    dark = false,
    scheme = lightColorScheme(
        primary = Color(0xFF00796B),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD9F2EC),
        onPrimaryContainer = Color(0xFF00574C),
        secondary = Color(0xFF00796B),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD9F2EC),
        onSecondaryContainer = Color(0xFF00574C),
        background = Color(0xFFEDF3F0),
        onBackground = Color(0xFF15201D),
        surface = Color.White,
        onSurface = Color(0xFF15201D),
        surfaceVariant = Color(0xFFE8F0EC),
        onSurfaceVariant = Color(0xFF4F5F5A),
        outline = Color(0xFF7E8C87),
        outlineVariant = Color(0xFFC2D0CA),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF4F9F7),
        surfaceContainer = Color(0xFFEDF3F0),
        surfaceContainerHigh = Color.White,
        surfaceContainerHighest = Color(0xFFE8F0EC),
        error = Color(0xFFC62828)
    ),
    header = Color(0xFF0F4C45),
    totalsBg = Color(0xFF1D4A44),
    totalsBlue = Color(0xFF8FD9E8),
    totalsGreen = Color(0xFF6FE3A5),
    payGreen = Color(0xFF2E7D32),
    negative = Color(0xFFFFB4A9)
)

private val MidnightDark = AppPalette(
    dark = true,
    scheme = darkColorScheme(
        primary = Color(0xFF8AB4F8),
        onPrimary = Color(0xFF0A2547),
        primaryContainer = Color(0xFF233656),
        onPrimaryContainer = Color(0xFFB3D0FB),
        secondary = Color(0xFF8AB4F8),
        onSecondary = Color(0xFF0A2547),
        secondaryContainer = Color(0xFF233656),
        onSecondaryContainer = Color(0xFFB3D0FB),
        background = Color(0xFF0D1320),
        onBackground = Color(0xFFE8EBF2),
        surface = Color(0xFF161E2E),
        onSurface = Color(0xFFE8EBF2),
        surfaceVariant = Color(0xFF202A3E),
        onSurfaceVariant = Color(0xFFA4ADBF),
        outline = Color(0xFF6F7A90),
        outlineVariant = Color(0xFF2C3850),
        surfaceContainerLowest = Color(0xFF0B1018),
        surfaceContainerLow = Color(0xFF121A28),
        surfaceContainer = Color(0xFF161E2E),
        surfaceContainerHigh = Color(0xFF1D2739),
        surfaceContainerHighest = Color(0xFF26324A),
        error = Color(0xFFFF8A80)
    ),
    header = Color(0xFF111C33),
    totalsBg = Color(0xFF23314D),
    totalsBlue = Color(0xFF8AB4F8),
    totalsGreen = Color(0xFF5BD68F),
    payGreen = Color(0xFF5BD68F),
    negative = Color(0xFFFF8A80)
)

private val BlackDark = AppPalette(
    dark = true,
    scheme = darkColorScheme(
        primary = Color(0xFF5AC8FA),
        onPrimary = Color(0xFF00293A),
        primaryContainer = Color(0xFF0F3A4D),
        onPrimaryContainer = Color(0xFFA6E1FB),
        secondary = Color(0xFF5AC8FA),
        onSecondary = Color(0xFF00293A),
        secondaryContainer = Color(0xFF0F3A4D),
        onSecondaryContainer = Color(0xFFA6E1FB),
        background = Color(0xFF000000),
        onBackground = Color(0xFFF2F2F7),
        surface = Color(0xFF111113),
        onSurface = Color(0xFFF2F2F7),
        surfaceVariant = Color(0xFF1C1C1E),
        onSurfaceVariant = Color(0xFF9D9DA5),
        outline = Color(0xFF6C6C70),
        outlineVariant = Color(0xFF2C2C2E),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF0A0A0B),
        surfaceContainer = Color(0xFF111113),
        surfaceContainerHigh = Color(0xFF1C1C1E),
        surfaceContainerHighest = Color(0xFF2C2C2E),
        error = Color(0xFFFF6961)
    ),
    header = Color(0xFF000000),
    totalsBg = Color(0xFF1C1C1E),
    totalsBlue = Color(0xFF5AC8FA),
    totalsGreen = Color(0xFF30D158),
    payGreen = Color(0xFF30D158),
    negative = Color(0xFFFF6961)
)

val LightPalettes: List<AppPalette> = listOf(ClassicLight, MintLight)
val DarkPalettes: List<AppPalette> = listOf(MidnightDark, BlackDark)

val LocalPalette = staticCompositionLocalOf { ClassicLight }
val LocalStrings = staticCompositionLocalOf { Strings(false) }

@Composable
fun HoursTrackerTheme(settings: Settings, content: @Composable () -> Unit) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val palette = if (dark) {
        DarkPalettes[settings.darkTheme.coerceIn(0, DarkPalettes.lastIndex)]
    } else {
        LightPalettes[settings.lightTheme.coerceIn(0, LightPalettes.lastIndex)]
    }
    val strings = remember(settings.language) { Strings(settings.language == "uk") }

    CompositionLocalProvider(LocalPalette provides palette, LocalStrings provides strings) {
        MaterialTheme(colorScheme = palette.scheme, content = content)
    }
}
