package com.greenicephoenix.traceledger.core.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NothingRed,
    background = BlackPure,
    surface = Grey850,
    onPrimary = WhitePure,
    onBackground = WhitePure,
    onSurface = WhitePure,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = NothingRed,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = WhitePure,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    error = ErrorRed
)


/**
 * Root theme for TraceLedger
 */
@Composable
fun TraceLedgerTheme(
    themeMode: ThemeMode,
    content: @Composable () -> Unit
) {
    val colors = when (themeMode) {
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = TraceLedgerTypography,
        content = content
    )
}
