package com.faiqbaig.metabolic.core.ui.theme


import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = MetabolicGreen,
    onPrimary          = NeutralWhite,
    primaryContainer   = MetabolicGreenLight,
    onPrimaryContainer = NeutralTextPrimary,
    secondary          = MetabolicCyan,
    onSecondary        = NeutralWhite,
    secondaryContainer = Color(0xFFCCF0FF),
    onSecondaryContainer = NeutralTextPrimary,
    background         = NeutralOffWhite,
    onBackground       = NeutralTextPrimary,
    surface            = NeutralWhite,
    onSurface          = NeutralTextPrimary,
    surfaceVariant     = NeutralSurface,
    onSurfaceVariant   = NeutralTextSecondary,
    outline            = NeutralBorder,
    error              = SemanticError,
    onError            = NeutralWhite
)

private val DarkColorScheme = darkColorScheme(
    primary            = MetabolicGreen,
    onPrimary          = DarkBackground,
    primaryContainer   = MetabolicGreenDark,
    onPrimaryContainer = DarkTextPrimary,
    secondary          = MetabolicCyan,
    onSecondary        = DarkBackground,
    secondaryContainer = MetabolicCyanDark,
    onSecondaryContainer = DarkTextPrimary,
    background         = DarkBackground,
    onBackground       = DarkTextPrimary,
    surface            = DarkSurface,
    onSurface          = DarkTextPrimary,
    surfaceVariant     = DarkSurfaceVariant,
    onSurfaceVariant   = DarkTextSecondary,
    outline            = DarkBorder,
    error              = SemanticError,
    onError            = DarkBackground
)

@Composable
fun MetabolicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MetabolicTypography,
        content     = content
    )
}