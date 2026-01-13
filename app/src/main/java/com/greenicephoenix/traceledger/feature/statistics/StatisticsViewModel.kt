package com.greenicephoenix.traceledger.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.domain.model.TransactionType
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import java.time.YearMonth

class StatisticsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // ─────────────────────────────────────────────
    // Selected month (shared concept with History)
    // ─────────────────────────────────────────────

    private val _selectedMonth =
        MutableStateFlow(YearMonth.now())

    val selectedMonth: StateFlow<YearMonth> =
        _selectedMonth.asStateFlow()

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }


    // ─────────────────────────────────────────────
    // Monthly aggregates (TRANSACTION-DERIVED)
    // ─────────────────────────────────────────────

    private val monthlyTransactions =
        combine(
            transactionRepository.observeTransactions(),
            _selectedMonth
        ) { transactions, month ->
            transactions.filter {
                YearMonth.from(it.date) == month
            }
        }

    // ─────────────────────────────────────────────
    // Analytics-safe monthly transactions
    // (EXCLUDES transfers by design)
    // ─────────────────────────────────────────────

    private val monthlyAnalyticsTransactions =
        monthlyTransactions.map { list ->
            list.filter {
                it.type == TransactionType.EXPENSE ||
                        it.type == TransactionType.INCOME
            }
        }

    val totalIncome: StateFlow<BigDecimal> =
        monthlyAnalyticsTransactions
            .map { list ->
                list
                    .filter { it.type == TransactionType.INCOME }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BigDecimal.ZERO
            )

    val totalExpense: StateFlow<BigDecimal> =
        monthlyAnalyticsTransactions
            .map { list ->
                list
                    .filter { it.type == TransactionType.EXPENSE }
                    .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BigDecimal.ZERO
            )

    val netAmount: StateFlow<BigDecimal> =
        combine(
            totalIncome,
            totalExpense
        ) { income, expense ->
            income.subtract(expense)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BigDecimal.ZERO
        )

    // ─────────────────────────────────────────────
    // Category breakdowns (MONTHLY)
    // ─────────────────────────────────────────────

    /**
     * Monthly EXPENSE totals grouped by categoryId.
     * Transfers and null categories are excluded.
     */
    val expenseByCategory: StateFlow<Map<String, BigDecimal>> =
        monthlyAnalyticsTransactions
            .map { list ->
                list
                    .asSequence()
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                                it.categoryId != null
                    }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, transactions) ->
                        transactions.fold(BigDecimal.ZERO) { acc, tx ->
                            acc + tx.amount
                        }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    /**
     * Monthly INCOME totals grouped by categoryId.
     * Null categories are excluded.
     */
    val incomeByCategory: StateFlow<Map<String, BigDecimal>> =
        monthlyAnalyticsTransactions
            .map { list ->
                list
                    .asSequence()
                    .filter {
                        it.type == TransactionType.INCOME &&
                                it.categoryId != null
                    }
                    .groupBy { it.categoryId!! }
                    .mapValues { (_, transactions) ->
                        transactions.fold(BigDecimal.ZERO) { acc, tx ->
                            acc + tx.amount
                        }
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    // ─────────────────────────────────────────────
    // UI-ready helpers: sorting & percentages
    // ─────────────────────────────────────────────

    /**
     * Represents a category slice ready for UI.
     */
    data class CategorySlice(
        val categoryId: String,
        val amount: BigDecimal,
        val percentage: Float
    )

    private fun buildRawSlices(
        totals: Map<String, BigDecimal>
    ): List<CategorySlice> {

        if (totals.isEmpty()) return emptyList()

        val totalAmount =
            totals.values.fold(BigDecimal.ZERO) { acc, v -> acc + v }

        if (totalAmount.compareTo(BigDecimal.ZERO) == 0)
            return emptyList()

        return totals
            .map { (categoryId, amount) ->
                val pct =
                    amount
                        .multiply(BigDecimal(100))
                        .divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                        .toFloat()

                CategorySlice(
                    categoryId = categoryId,
                    amount = amount,
                    percentage = pct
                )
            }
            .sortedByDescending { it.amount }
    }

}