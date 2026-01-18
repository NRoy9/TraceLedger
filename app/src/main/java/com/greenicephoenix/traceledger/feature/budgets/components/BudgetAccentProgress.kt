package com.greenicephoenix.traceledger.feature.budgets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState
import com.greenicephoenix.traceledger.feature.budgets.ui.BudgetColors

@Composable
fun BudgetAccentProgress(
    progress: Float,
    state: BudgetState,
    modifier: Modifier = Modifier
) {
    val clamped = progress.coerceIn(0f, 1f)

    val accentColor = BudgetColors.accent(state)


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(4.dp)
                .background(accentColor, RoundedCornerShape(2.dp))
        )
    }
}
