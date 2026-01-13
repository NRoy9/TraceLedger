package com.greenicephoenix.traceledger.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
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

    private val accountNameMap =
        MutableStateFlow<Map<String, String>>(emptyMap())

    private val categoryNameMap =
        MutableStateFlow<Map<String, String>>(emptyMap())

    private val referenceData: StateFlow<Pair<Map<String, String>, Map<String, String>>> =
        combine(
            accountNameMap,
            categoryNameMap
        ) { accounts, categories ->
            accounts to categories
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyMap<String, String>() to emptyMap()
        )

    val visibleTransactions: StateFlow<List<TransactionUiModel>> =
        combine(
            transactionRepository.observeTransactions(),
            _selectedMonth,
            _typeFilter,
            _searchQuery,
            referenceData
        ) { transactions, month, typeFilter, query, ref ->

            val (accounts, categories) = ref
            val q = query.trim().lowercase()

            transactions
                .asSequence()
                .filter { YearMonth.from(it.date) == month }
                .filter { typeFilter == null || it.type == typeFilter }
                .filter { tx ->
                    if (q.isBlank()) return@filter true

                    val accountMatch =
                        tx.fromAccountId?.let { accounts[it] }?.contains(q) == true ||
                                tx.toAccountId?.let { accounts[it] }?.contains(q) == true

                    val categoryMatch =
                        tx.categoryId?.let { categories[it] }?.contains(q) == true

                    val amountMatch =
                        tx.amount.toPlainString().contains(q)

                    val notesMatch =
                        tx.note?.lowercase()?.contains(q) == true

                    val dateMatch =
                        tx.date.toString().contains(q)

                    val typeMatch =
                        tx.type.name.lowercase().contains(q)

                    accountMatch ||
                            categoryMatch ||
                            amountMatch ||
                            notesMatch ||
                            dateMatch ||
                            typeMatch
                }
                .sortedByDescending { it.date }
                .toList()

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )


    val totalIn: StateFlow<BigDecimal> =
        visibleTransactions.map { list ->
            list
                .filter { it.type == TransactionType.INCOME }
                .fold(BigDecimal.ZERO) { acc, tx -> acc + tx.amount }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BigDecimal.ZERO
        )

    val totalOut: StateFlow<BigDecimal> =
        visibleTransactions.map { list ->
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

    fun setAccounts(accounts: List<AccountUiModel>) {
        accountNameMap.value =
            accounts.associate { it.id to it.name.lowercase() }
    }

    fun setCategories(categories: List<CategoryUiModel>) {
        categoryNameMap.value =
            categories.associate { it.id to it.name.lowercase() }
    }

}