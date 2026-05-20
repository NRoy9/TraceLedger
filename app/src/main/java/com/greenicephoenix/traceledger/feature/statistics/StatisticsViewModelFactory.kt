package com.greenicephoenix.traceledger.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.greenicephoenix.traceledger.core.repository.AccountRepository
import com.greenicephoenix.traceledger.core.repository.RecurringTransactionRepository
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetRepository

class StatisticsViewModelFactory(
    private val transactionRepository:  TransactionRepository,
    private val budgetRepository:       BudgetRepository,
    private val accountRepository:      AccountRepository,
    private val recurringRepository:    RecurringTransactionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            return StatisticsViewModel(
                transactionRepository = transactionRepository,
                budgetRepository      = budgetRepository,
                accountRepository     = accountRepository,
                recurringRepository   = recurringRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}