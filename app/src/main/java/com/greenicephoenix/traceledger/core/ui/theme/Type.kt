package com.greenicephoenix.traceledger.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.greenicephoenix.traceledger.R

/**
 * Custom font families
 */

val DotMatrixFont = FontFamily(
    Font(R.font.dot_matrix, FontWeight.Normal)
)

/**
 * TraceLedger Typography
 */
val TraceLedgerTypography = Typography(

    // App titles, screen headers
    headlineLarge = TextStyle(
        fontFamily = DotMatrixFont,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = DotMatrixFont,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),

    // Section titles
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),

    // Body text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp
    ),

    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp
    )
)
