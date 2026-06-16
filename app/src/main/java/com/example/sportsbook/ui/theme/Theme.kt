package com.example.sportsbook.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════
// SportsBooks v3 Dark Theme
// Design: #121212 bg, #1E1E1E surfaces, #4CAF50 green accent,
// #2196F3 blue secondary. Matches the mockup spec.
// ══════════════════════════════════════════════════════════════

private val SportsBookDarkColorScheme = darkColorScheme(
    primary = GreenAccent,                     // #4CAF50 — buttons, key actions
    onPrimary = Color.White,                   // white text on green
    primaryContainer = GreenDark,              // #2E7D32 — container variant
    onPrimaryContainer = Color.White,
    secondary = BlueAccent,                    // #2196F3 — secondary actions
    onSecondary = Color.White,
    secondaryContainer = BlueDarkAccent,       // #1565C0
    onSecondaryContainer = Color.White,
    tertiary = OrangeAccent,                   // #FF9800 — warning/pending
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF5C3D00),
    onTertiaryContainer = OrangeAccent,
    background = DarkBg,                       // #121212
    onBackground = DarkTextPrimary,            // white
    surface = DarkSurface,                     // #1E1E1E — cards, sheets
    onSurface = DarkTextPrimary,               // white
    surfaceVariant = DarkSurfaceLight,         // #252525
    onSurfaceVariant = DarkTextSecondary,      // #888888
    surfaceContainerHighest = DarkSurfaceLight,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkSurfaceLight,
    surfaceContainerLow = DarkBg,
    surfaceContainerLowest = DarkBg,
    surfaceTint = Color.Transparent,           // no tint overlay
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF5C1A1A),
    onErrorContainer = Color(0xFFFFCDD2),
    outline = DarkBorder,                      // #2A2A2A
    outlineVariant = Color(0xFF333333),
    inverseSurface = Color.White,
    inverseOnSurface = DarkBg,
    inversePrimary = GreenDark
)

// Legacy light scheme kept for potential future toggle
private val SportsBookLightColorScheme = lightColorScheme(
    primary = DarkBg,
    onPrimary = GreenAccent,
    primaryContainer = GreenAccent,
    onPrimaryContainer = DarkBg,
    secondary = DarkTextSecondary,
    onSecondary = DarkSurface,
    secondaryContainer = Color(0xFFF3F4F6),
    onSecondaryContainer = DarkTextPrimary,
    tertiary = SportGreen,
    onTertiary = DarkSurface,
    tertiaryContainer = EmeraldLight,
    onTertiaryContainer = EmeraldDark,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkBg,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerHighest = DarkSurface,
    surfaceContainer = DarkBg,
    surfaceContainerHigh = DarkSurface,
    surfaceContainerLow = DarkBg,
    surfaceContainerLowest = DarkSurface,
    surfaceTint = Color.Transparent,
    error = ErrorRed,
    onError = DarkSurface,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    outline = DarkBorder,
    outlineVariant = DividerGray,
    inverseSurface = DarkBg,
    inverseOnSurface = DarkSurface,
    inversePrimary = GreenAccent
)

@Composable
fun SportsBookTheme(
    darkTheme: Boolean = true, // v3: dark-first
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SportsBookDarkColorScheme else SportsBookLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = if (darkTheme) DarkBg.toArgb() else DarkBg.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = if (darkTheme) DarkNavBar.toArgb() else DarkSurface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
