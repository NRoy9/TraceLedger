package com.greenicephoenix.traceledger.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetState
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetStatus
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetRepository
import com.greenicephoenix.traceledger.feature.budgets.domain.BudgetSignal
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * BudgetsViewModel
 *
 * RULES:
 * - Read-only evaluation
 * - Expense categories only
 * - Month-specific
 * - Derived entirely from Room + Transactions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetsViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedMonth =
        MutableStateFlow(YearMonth.now())

    val selectedMonth: StateFlow<YearMonth> =
        _selectedMonth.asStateFlow()

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    private val budgetsForMonth =
        selectedMonth.flatMapLatest { month ->
            budgetRepository.observeBudgetsForMonth(month)
        }

    private val expenseTransactionsForMonth =
        combine(
            transactionRepository.observeTransactions(),
            selectedMonth
        ) { transactions, month ->
            transactions.filter {
                it.type == TransactionType.EXPENSE &&
                        YearMonth.from(it.date) == month
            }
        }

    val budgetStatuses: StateFlow<List<BudgetStatus>> =
        combine(
            budgetsForMonth,
            expenseTransactionsForMonth
        ) { budgets, transactions ->

            budgets
                .filter { it.isActive }
                .map { budget ->

                    val used =
                        transactions
                            .filter { it.categoryId == budget.categoryId }
                            .fold(BigDecimal.ZERO) { acc, tx ->
                                acc + tx.amount
                            }

                    val remaining =
                        budget.limitAmount.subtract(used)

                    val progress =
                        if (budget.limitAmount > BigDecimal.ZERO)
                            used
                                .divide(
                                    budget.limitAmount,
                                    4,
                                    RoundingMode.HALF_UP
                                )
                                .toFloat()
                        else 0f

                    val state =
                        when {
                            progress >= 1f -> BudgetState.EXCEEDED
                            progress >= 0.7f -> BudgetState.WARNING
                            else -> BudgetState.SAFE
                        }

                    BudgetStatus(
                        budgetId = budget.id,
                        categoryId = budget.categoryId,
                        month = budget.month,
                        limit = budget.limitAmount,
                        used = used,
                        remaining = remaining,
                        progress = progress,
                        state = state
                    )
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val budgetSignals: StateFlow<List<BudgetSignal>> =
        budgetStatuses
            .map { statuses ->
                statuses.mapNotNull { status ->
                    when (status.state) {
                        BudgetState.WARNING -> {
                            BudgetSignal.ApproachingLimit(
                                budgetId = status.budgetId,
                                categoryId = status.categoryId,
                                month = status.month,
                                progress = status.progress
                            )
                        }

                        BudgetState.EXCEEDED -> {
                            BudgetSignal.Exceeded(
                                budgetId = status.budgetId,
                                categoryId = status.categoryId,
                                month = status.month,
                                overspentAmount = status.used - status.limit
                            )
                        }

                        else -> null
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val hasExceededBudgets: StateFlow<Boolean> =
        budgetSignals
            .map { signals ->
                signals.any { it is BudgetSignal.Exceeded }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val exceededBudgetsCount: StateFlow<Int> =
        budgetSignals
            .map { signals ->
                signals.count { it is BudgetSignal.Exceeded }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budgetId)
        }
    }

}
