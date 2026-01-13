package com.greenicephoenix.traceledger.core.di

import android.content.Context
import com.greenicephoenix.traceledger.core.database.TraceLedgerDatabase
import com.greenicephoenix.traceledger.core.repository.AccountRepository
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.feature.statistics.StatisticsViewModelFactory

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

    val statisticsViewModelFactory =
        StatisticsViewModelFactory(transactionRepository)

}