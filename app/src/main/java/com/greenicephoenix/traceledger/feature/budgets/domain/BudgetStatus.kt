package com.greenicephoenix.traceledger.feature.budgets.domain

import java.math.BigDecimal
import java.time.YearMonth

/**
 * BudgetStatus
 *
 * Fully derived, UI-agnostic budget evaluation.
 */
data class BudgetStatus(
    val budgetId: String,
    val categoryId: String,
    val month: YearMonth,
    val limit: BigDecimal,
    val used: BigDecimal,
    val remaining: BigDecimal,
    val progress: Float,
    val state: BudgetState
)
