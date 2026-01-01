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

/**
 * Root theme for TraceLedger
 */
@Composable
fun TraceLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = TraceLedgerTypography,
        content = content
    )
}
