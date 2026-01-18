package com.greenicephoenix.traceledger.feature.budgets.domain

/**
 * BudgetState
 *
 * Visual-only semantic state.
 * No behavior attached.
 */
enum class BudgetState {
    SAFE,       // < 70%
    WARNING,    // 70% – 100%
    EXCEEDED    // > 100%
}
