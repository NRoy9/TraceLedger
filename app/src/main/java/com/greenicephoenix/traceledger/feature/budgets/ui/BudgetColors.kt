package com.greenicephoenix.traceledger.feature.budgets.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState

object BudgetColors {

    @Composable
    fun accent(state: BudgetState): Color =
        when (state) {
            BudgetState.SAFE -> Color(0xFF4CAF50)
            BudgetState.WARNING -> Color(0xFFFFC107)
            BudgetState.EXCEEDED -> NothingRed
        }

    @Composable
    fun cardBackground(state: BudgetState): Color {
        val surface = MaterialTheme.colorScheme.surface
        val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f

        return when (state) {
            BudgetState.SAFE ->
                surface

            BudgetState.WARNING ->
                if (isLight)
                    Color(0xFFFFF8E1)   // soft amber tint
                else
                    Color(0xFF1A160F)

            BudgetState.EXCEEDED ->
                if (isLight)
                    Color(0xFFFFEBEE)   // soft red tint
                else
                    Color(0xFF1A0F0F)
        }
    }

    @Composable
    fun remainingText(state: BudgetState): Color =
        when (state) {
            BudgetState.EXCEEDED -> NothingRed
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        }
}