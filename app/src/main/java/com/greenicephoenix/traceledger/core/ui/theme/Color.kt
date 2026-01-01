package com.greenicephoenix.traceledger.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TraceLedger Color System
 * Inspired by Nothing OS
 *
 * Rules:
 * - Pure blacks
 * - Soft greys
 * - High contrast whites
 * - One controlled red accent
 */

val BlackPure = Color(0xFF000000)
val WhitePure = Color(0xFFFFFFFF)

// Greys (UI surfaces)
val Grey900 = Color(0xFF0D0D0D)
val Grey850 = Color(0xFF141414)
val Grey800 = Color(0xFF1A1A1A)
val Grey700 = Color(0xFF2A2A2A)
val Grey600 = Color(0xFF3A3A3A)

// Text
val TextPrimary = WhitePure
val TextSecondary = Color(0xFFB3B3B3)
val TextDisabled = Color(0xFF6F6F6F)

// Accent (Nothing Red)
val NothingRed = Color(0xFFE53935)

// Status colors
val SuccessGreen = Color(0xFF4CAF50)
val ErrorRed = NothingRed
