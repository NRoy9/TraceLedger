package com.greenicephoenix.traceledger.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.time.YearMonth
import java.math.RoundingMode

/**
 * StatisticsViewModel
 *
 * RULES (NON-NEGOTIABLE):
 * - Read-only
 * - Derived ONLY from transactions
 * - Transfers are excluded
 * - No account balance usage
 * - No UI or chart logic
 */
class StatisticsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    /**
     * UI-agnostic category slice.
     * Suitable for charts and insights.
     */
    data class CategorySlice(
        val categoryId: String,
        val amount: BigDecimal,
        val percentage: Float
    )

    /**
     * CashflowEntry
     * Represents daily income vs expense for the selected month.
     */
    data class CashflowEntry(
        val day: Int,
        val income: BigDecimal,
        val expense: BigDecimal
    )

    /**
     * Monthly category expense trend entry.
     */
    data class CategoryMonthlyTrend(
        val categoryId: String,
        val month: YearMonth,
        val total: BigDecimal
    )


    // ─────────────────────────────────────────────
    // Selected month
    // ─────────────────────────────────────────────

    private val _selectedMonth =
        MutableStateFlow(YearMonth.now())

    val selectedMonth: StateFlow<YearMonth> =
        _selectedMonth.asStateFlow()

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    // ─────────────────────────────────────────────
    // Monthly transaction stream
    // ─────────────────────────────────────────────

    private val monthlyTransactions =
        combine(
            transactionRepository.observeTransactions(),
            selectedMonth
        ) { transactions, month ->
            transactions.filter {
                YearMonth.from(it.date) == month
            }
        }

    // ─────────────────────────────────────────────
    // Analytics-safe transactions
    // ─────────────────────────────────────────────

    private val analyticsTransactions =
        monthlyTransactions.map { list ->
            list.filter {
                it.type == TransactionType.EXPENSE ||
                        it.type == TransactionType.INCOME
            }
        }

    // ─────────────────────────────────────────────
    // Aggregates
    // ─────────────────────────────────────────────

    val totalIncome: StateFlow<BigDecimal> =
        analyticsTransactions
            .map { list ->
                list.filter { it.type == TransactionType.INCOME }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BigDecimal.ZERO)

    val totalExpense: StateFlow<BigDecimal> =
        analyticsTransactions
            .map { list ->
                list.filter { it.type == TransactionType.EXPENSE }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BigDecimal.ZERO)

    val netAmount: StateFlow<BigDecimal> =
        combine(totalIncome, totalExpense) { income, expense ->
            income.subtract(expense)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BigDecimal.ZERO)

    // ─────────────────────────────────────────────
    // Category breakdowns
    // ─────────────────────────────────────────────

    val expenseByCategory: StateFlow<Map<String, BigDecimal>> =
        analyticsTransactions
            .map { txs ->
                txs.filter { it.type == TransactionType.EXPENSE && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, list) ->
                        list.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val incomeByCategory: StateFlow<Map<String, BigDecimal>> =
        analyticsTransactions
            .map { txs ->
                txs.filter { it.type == TransactionType.INCOME && it.categoryId != null }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, list) ->
                        list.fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // ─────────────────────────────────────────────
    // UI-ready slices
    // ─────────────────────────────────────────────

    val expenseCategorySlices: StateFlow<List<CategorySlice>> =
        expenseByCategory
            .map { buildCategorySlices(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val incomeCategorySlices: StateFlow<List<CategorySlice>> =
        incomeByCategory
            .map { buildCategorySlices(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ─────────────────────────────────────────────
    // Internal helper (MUST be inside ViewModel)
    // ─────────────────────────────────────────────

    private fun buildCategorySlices(
        totals: Map<String, BigDecimal>
    ): List<CategorySlice> {

        if (totals.isEmpty()) return emptyList()

        val totalAmount =
            totals.values.fold(BigDecimal.ZERO) { acc, v -> acc + v }

        if (totalAmount == BigDecimal.ZERO) return emptyList()

        return totals
            .map { (categoryId, amount) ->
                val percentage =
                    amount
                        .multiply(BigDecimal(100))
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .toFloat()

                CategorySlice(
                    categoryId = categoryId,
                    amount = amount,
                    percentage = percentage
                )
            }
            .sortedByDescending { it.amount }
    }

    /**
     * Daily cashflow for the selected month.
     *
     * Rules:
     * - Derived ONLY from transactions
     * - Transfers excluded
     * - Grouped by day of month
     */
    val cashflowByDay: StateFlow<List<CashflowEntry>> =
        analyticsTransactions
            .map { transactions ->

                transactions
                    .groupBy { it.date.dayOfMonth }
                    .map { (day, dayTransactions) ->

                        val income =
                            dayTransactions
                                .filter { it.type == TransactionType.INCOME }
                                .fold(BigDecimal.ZERO) { acc, tx ->
                                    acc + tx.amount
                                }

                        val expense =
                            dayTransactions
                                .filter { it.type == TransactionType.EXPENSE }
                                .fold(BigDecimal.ZERO) { acc, tx ->
                                    acc + tx.amount
                                }

                        CashflowEntry(
                            day = day,
                            income = income,
                            expense = expense
                        )
                    }
                    .sortedBy { it.day }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Expense trends per category across months.
     *
     * Rules:
     * - Expense only
     * - Transfers excluded
     * - Grouped by YearMonth
     */
    val categoryExpenseTrends: StateFlow<List<CategoryMonthlyTrend>> =
        transactionRepository.observeTransactions()
            .map { transactions ->

                transactions
                    .asSequence()
                    .filter { it.type == TransactionType.EXPENSE }
                    .filter { it.categoryId != null }
                    .groupBy {
                        Pair(
                            it.categoryId!!,
                            YearMonth.from(it.date)
                        )
                    }
                    .map { (key, list) ->
                        val (categoryId, month) = key

                        CategoryMonthlyTrend(
                            categoryId = categoryId,
                            month = month,
                            total = list.fold(BigDecimal.ZERO) { acc, tx ->
                                acc + tx.amount
                            }
                        )
                    }
                    .sortedWith(
                        compareBy<CategoryMonthlyTrend> { it.categoryId }
                            .thenBy { it.month }
                    )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

}
