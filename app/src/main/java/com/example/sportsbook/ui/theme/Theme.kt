package com.example.sportsbook.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════
// SportsBooks Light Theme
// Design: gray-50 backgrounds, white cards, dark nav bar
// with gold (yellow-300) accents. Matches the mockup spec.
// ══════════════════════════════════════════════════════════════

private val SportsBookColorScheme = lightColorScheme(
    primary = NavBarBg,                    // gray-900 — buttons, key actions
    onPrimary = GoldAccent,                // yellow-300 on dark primary
    primaryContainer = GoldAccent,         // gold chip/badge bg
    onPrimaryContainer = NavBarBg,         // dark text on gold container
    secondary = TextSecondary,             // gray-500
    onSecondary = CardWhite,               // white
    secondaryContainer = Color(0xFFF3F4F6), // gray-100
    onSecondaryContainer = TextPrimary,    // gray-900
    tertiary = SportGreen,                 // emerald-500 — success/active
    onTertiary = CardWhite,
    tertiaryContainer = EmeraldLight,      // emerald-100
    onTertiaryContainer = EmeraldDark,     // emerald-700
    background = LightBg,                  // gray-50
    onBackground = TextPrimary,            // gray-900
    surface = CardWhite,                   // white — cards, sheets
    onSurface = TextPrimary,              // gray-900
    surfaceVariant = LightBg,             // gray-50
    onSurfaceVariant = TextSecondary,     // gray-500
    surfaceContainerHighest = CardWhite,
    surfaceContainer = LightBg,
    surfaceContainerHigh = CardWhite,
    surfaceContainerLow = LightBg,
    surfaceContainerLowest = CardWhite,
    surfaceTint = Color.Transparent,       // no tint on light cards
    error = ErrorRed,
    onError = CardWhite,
    errorContainer = Color(0xFFFEE2E2),    // red-100
    onErrorContainer = Color(0xFF991B1B),  // red-800
    outline = BorderGray,                  // gray-200
    outlineVariant = DividerGray,          // gray-100
    inverseSurface = NavBarBg,            // gray-900
    inverseOnSurface = CardWhite,
    inversePrimary = GoldAccent
)

@Composable
fun SportsBookTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = NavBarBg.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = CardWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = SportsBookColorScheme,
        typography = Typography,
        content = content
    )
}
