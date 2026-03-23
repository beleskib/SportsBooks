package com.example.sportsbook.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SportsBookColorScheme = darkColorScheme(
    primary = USOpenGold,
    onPrimary = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = WarmWhite,
    secondary = Navy300,
    onSecondary = WarmWhite,
    secondaryContainer = Navy600,
    onSecondaryContainer = WarmWhite,
    tertiary = SportGreen,
    onTertiary = Navy900,
    tertiaryContainer = Navy600,
    onTertiaryContainer = WarmWhite,
    background = Navy900,
    onBackground = WarmWhite,
    surface = Navy800,
    onSurface = WarmWhite,
    surfaceVariant = Navy700,
    onSurfaceVariant = CoolGray,
    surfaceContainerHighest = Navy600,
    surfaceContainer = Navy700,
    surfaceContainerHigh = Navy600,
    surfaceContainerLow = Navy800,
    surfaceContainerLowest = Navy900,
    surfaceTint = USOpenGold,
    error = CoralRed,
    onError = WarmWhite,
    errorContainer = CoralRedDark,
    onErrorContainer = WarmWhite,
    outline = Navy400,
    outlineVariant = Navy500,
    inverseSurface = WarmWhite,
    inverseOnSurface = Navy900,
    inversePrimary = GoldDark
)

@Composable
fun SportsBookTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = Navy900.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Navy900.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = SportsBookColorScheme,
        typography = Typography,
        content = content
    )
}
