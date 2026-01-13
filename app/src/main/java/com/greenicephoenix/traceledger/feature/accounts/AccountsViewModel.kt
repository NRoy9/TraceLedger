package com.greenicephoenix.traceledger.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.repository.AccountRepository
import com.greenicephoenix.traceledger.core.repository.TransactionRepository
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.greenicephoenix.traceledger.TraceLedgerApp


class AccountsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app =
        getApplication<TraceLedgerApp>()

    private val accountRepository =
        app.container.accountRepository

    private val transactionRepository =
        app.container.transactionRepository

    val accounts: StateFlow<List<AccountUiModel>> =
        accountRepository.observeAccounts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addAccount(account: AccountUiModel) {
        viewModelScope.launch {
            accountRepository.upsert(account)
        }
    }

    fun updateAccount(account: AccountUiModel) {
        viewModelScope.launch {
            accountRepository.upsert(account)
        }
    }

    fun deleteAccount(accountId: String) {
        viewModelScope.launch {
            accountRepository.delete(accountId)
        }
    }
}