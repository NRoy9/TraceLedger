package com.greenicephoenix.traceledger.feature.budgets.data

import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

/**
 * BudgetRepository
 *
 * Thin abstraction over BudgetDao.
 * No business logic here.
 */
class BudgetRepository(
    private val budgetDao: BudgetDao
) {

    fun observeBudgetsForMonth(month: YearMonth): Flow<List<BudgetEntity>> {
        return budgetDao.observeBudgetsForMonth(month)
    }

    suspend fun upsertBudget(budget: BudgetEntity) {
        budgetDao.upsertBudget(budget)
    }

    suspend fun deleteBudget(budgetId: String) {
        budgetDao.deleteBudget(budgetId)
    }
}
