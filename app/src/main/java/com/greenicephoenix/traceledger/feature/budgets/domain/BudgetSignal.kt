package com.greenicephoenix.traceledger.feature.budgets.domain

import java.time.YearMonth

sealed class BudgetSignal {

    data class ApproachingLimit(
        val budgetId: String,
        val categoryId: String,
        val month: YearMonth,
        val progress: Float
    ) : BudgetSignal()

    data class Exceeded(
        val budgetId: String,
        val categoryId: String,
        val month: YearMonth,
        val overspentAmount: java.math.BigDecimal
    ) : BudgetSignal()
}