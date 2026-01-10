package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.ui.graphics.Color

object CategoryColors {

    // ================= EXPENSE COLORS =================
    // Warm + neutral + alert tones (no duplicates)
    val expenseColors = listOf(
        Color(0xFFE53935), // red
        Color(0xFFD81B60), // pink
        Color(0xFF8E24AA), // purple
        Color(0xFF5E35B1), // deep purple
        Color(0xFF3949AB), // indigo
        Color(0xFF1E88E5), // blue
        Color(0xFF039BE5), // light blue
        Color(0xFF00897B), // teal

        Color(0xFF43A047), // green
        Color(0xFF7CB342), // light green
        Color(0xFFC0CA33), // lime
        Color(0xFFFDD835), // yellow
        Color(0xFFFFB300), // amber
        Color(0xFFFB8C00), // orange
        Color(0xFFF4511E), // deep orange
        Color(0xFF6D4C41)  // brown

    )

    // ================= INCOME COLORS =================
    // Cooler + positive + growth tones (completely different set)
    val incomeColors = listOf(
        Color(0xFF2E7D32), // dark green
        Color(0xFF388E3C),
        Color(0xFF00695C), // teal
        Color(0xFF00796B),
        Color(0xFF00838F), // cyan
        Color(0xFF0277BD), // blue
        Color(0xFF1565C0),
        Color(0xFF283593), // indigo

        Color(0xFF4527A0), // violet
        Color(0xFF512DA8),
        Color(0xFF6A1B9A), // purple
        Color(0xFFAD1457), // magenta
        Color(0xFF558B2F), // olive green
        Color(0xFF33691E), // deep green
        Color(0xFF006064), // deep cyan
        Color(0xFF37474F)  // blue grey
    )
}
