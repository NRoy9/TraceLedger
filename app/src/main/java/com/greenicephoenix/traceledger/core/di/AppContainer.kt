package com.greenicephoenix.traceledger.core.di

import android.content.Context
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.repository.AccountRepository
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModelFactory
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetRepository

class AppContainer(context: Context) {

    private val database = TraceLedgerDatabase.getInstance(context)

    val accountRepository: AccountRepository =
        AccountRepository(database.accountDao())

    val transactionRepository: TransactionRepository =
        TransactionRepository(
            database = database,
            transactionDao = database.transactionDao(),
            accountDao = database.accountDao()
        )

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(
            database.budgetDao()
        )
    }

    val statisticsViewModelFactory =
        StatisticsViewModelFactory(transactionRepository)

}