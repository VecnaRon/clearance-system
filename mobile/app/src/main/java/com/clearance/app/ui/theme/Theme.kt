package com.clearance.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ClearancePrimary,
    onPrimary = Color.White,
    secondary = ClearanceAccent,
    onSecondary = Color.White,
    tertiary = ClearanceInfo,
    onTertiary = Color.White,
    background = ClearanceLight,
    onBackground = ClearanceText,
    surface = Color.White,
    onSurface = ClearanceText,
    error = ClearanceDanger,
    onError = Color.White,
    surfaceVariant = ClearanceGray100,
    onSurfaceVariant = ClearanceGray600,
    outline = ClearanceBorder
)

private val DarkColors = darkColorScheme(
    primary = ClearancePrimary,
    onPrimary = Color.White,
    secondary = ClearanceAccent,
    onSecondary = Color.White,
    tertiary = ClearanceInfo,
    onTertiary = Color.White,
    background = ClearanceDark,
    onBackground = ClearanceGray100,
    surface = ClearanceDarkBg,
    onSurface = ClearanceGray100,
    error = ClearanceDanger,
    onError = Color.White,
    surfaceVariant = ClearanceGray700,
    onSurfaceVariant = ClearanceGray300,
    outline = ClearanceGray600
)

/**
 * UI STABILIZATION FIX: darkTheme now defaults to false instead of
 * isSystemInDarkTheme(). This app's screens hardcode dark text colors
 * (ClearanceTextPrimary/Secondary/Muted) on the assumption the
 * background is always light — the same assumption the original web
 * app makes (client/src/theme/theme.js has no dark-mode variant at
 * all). Following the phone's system dark-mode setting caused
 * Scaffold/AlertDialog/Snackbar backgrounds without an explicit
 * containerColor to silently become near-black while sitting under
 * that same hardcoded dark text — this was the actual cause of the
 * "dark text on dark background" bug reported on the Admin
 * Departments screen (and was a latent risk on every other screen
 * too, whether or not it had been noticed yet).
 *
 * DarkColors is left defined but unused by default, in case a real
 * dark-mode design is deliberately built later.
 */
@Composable
fun ClearanceSystemTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClearanceTypography,
        content = content
    )
}