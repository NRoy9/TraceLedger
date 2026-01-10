package com.greenicephoenix.traceledger.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import java.math.BigDecimal

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _selectedMonth =
        MutableStateFlow(YearMonth.now())

    val selectedMonth: StateFlow<YearMonth> =
        _selectedMonth.asStateFlow()

    private val _searchQuery =
        MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    private val _typeFilter =
        MutableStateFlow<TransactionType?>(null) // null = ALL

    val typeFilter: StateFlow<TransactionType?> =
        _typeFilter.asStateFlow()

    val monthlyTransactions: StateFlow<List<TransactionUiModel>> =
        combine(
            transactionRepository.observeTransactions(),
            _selectedMonth,
            _typeFilter
        ) { transactions, month, typeFilter ->

            transactions
                .asSequence()
                .filter { YearMonth.from(it.date) == month }
                .filter { tx ->
                    typeFilter == null || tx.type == typeFilter
                }
                .sortedByDescending { it.date }
                .toList()

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val totalIn: StateFlow<BigDecimal> =
        monthlyTransactions.map { list ->
            list
                .filter { it.type == TransactionType.INCOME }
                .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BigDecimal.ZERO
        )

    val totalOut: StateFlow<BigDecimal> =
        monthlyTransactions.map { list ->
            list
                .filter { it.type == TransactionType.EXPENSE }
                .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BigDecimal.ZERO
        )

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun goToPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun goToNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }


}