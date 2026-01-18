package com.greenicephoenix.traceledger.feature.budgets.ui

import androidx.compose.ui.graphics.Color
import com.greenicephoenix.traceledger.core.ui.theme.NothingRed
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState

object BudgetColors {

    fun accent(state: BudgetState): Color =
        when (state) {
            BudgetState.SAFE -> Color(0xFF4CAF50)
            BudgetState.WARNING -> Color(0xFFFFC107)
            BudgetState.EXCEEDED -> NothingRed
        }

    fun cardBackground(state: BudgetState): Color =
        when (state) {
            BudgetState.SAFE -> Color(0xFF141414)
            BudgetState.WARNING -> Color(0xFF1A160F)
            BudgetState.EXCEEDED -> Color(0xFF1A0F0F)
        }

    fun remainingText(state: BudgetState): Color =
        when (state) {
            BudgetState.EXCEEDED -> NothingRed
            else -> Color.Gray
        }
}