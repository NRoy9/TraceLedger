package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.ui.graphics.Color
import com.greenicephoenix.traceledger.domain.model.CategoryType

object CategoryColors {

    // ─────────────────────────────────────────────
    // EXPENSE
    // Warm / energetic / lifestyle tones
    // ─────────────────────────────────────────────

    val expenseColors = listOf(

        // Reds
        Color(0xFFE53935),
        Color(0xFFD32F2F),

        // Pinks
        Color(0xFFD81B60),
        Color(0xFFEC407A),

        // Purples
        Color(0xFF8E24AA),
        Color(0xFFAB47BC),

        // Deep Purples
        Color(0xFF5E35B1),
        Color(0xFF7E57C2),

        // Indigo
        Color(0xFF3949AB),

        // Blues
        Color(0xFF1E88E5),
        Color(0xFF42A5F5),

        // Teals
        Color(0xFF00897B),
        Color(0xFF26A69A),

        // Greens
        Color(0xFF43A047),
        Color(0xFF66BB6A),

        // Lime
        Color(0xFF7CB342),

        // Amber
        Color(0xFFFFB300),

        // Orange
        Color(0xFFFB8C00),
        Color(0xFFFF7043),

        // Browns
        Color(0xFF6D4C41),
        Color(0xFF8D6E63),

        // Blue Grey
        Color(0xFF546E7A)
    )

    // ─────────────────────────────────────────────
    // INCOME
    // Cooler / financial / growth tones
    // ─────────────────────────────────────────────

    val incomeColors = listOf(

        // Greens
        Color(0xFF2E7D32),
        Color(0xFF388E3C),
        Color(0xFF43A047),

        // Teals
        Color(0xFF00695C),
        Color(0xFF00796B),
        Color(0xFF00897B),

        // Cyan
        Color(0xFF00838F),
        Color(0xFF00ACC1),

        // Blues
        Color(0xFF0277BD),
        Color(0xFF1565C0),
        Color(0xFF1E88E5),

        // Indigo
        Color(0xFF283593),
        Color(0xFF3949AB),

        // Violet
        Color(0xFF4527A0),
        Color(0xFF512DA8),

        // Purple
        Color(0xFF6A1B9A),

        // Deep Green
        Color(0xFF33691E),

        // Deep Cyan
        Color(0xFF006064),

        // Slate
        Color(0xFF37474F)
    )

    // ─────────────────────────────────────────────
    // INVESTMENTS
    // Premium / wealth / asset tones
    // ─────────────────────────────────────────────

    val investmentColors = listOf(

        // Gold
        Color(0xFFF9A825),
        Color(0xFFFFB300),

        // Amber
        Color(0xFFF57F17),
        Color(0xFFFF8F00),

        // Bronze
        Color(0xFFE65100),

        // Emerald
        Color(0xFF2E7D32),
        Color(0xFF388E3C),

        // Teal
        Color(0xFF00695C),

        // Indigo
        Color(0xFF3949AB),
        Color(0xFF4527A0),

        // Purple
        Color(0xFF6A1B9A),

        // Browns
        Color(0xFF6D4C41),

        // Slate
        Color(0xFF455A64)
    )

    fun colorsFor(type: CategoryType): List<Color> {
        return when (type) {

            CategoryType.EXPENSE ->
                expenseColors

            CategoryType.INCOME ->
                incomeColors

            CategoryType.INVESTMENT ->
                investmentColors
        }
    }
}