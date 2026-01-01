package com.greenicephoenix.traceledger.feature.accounts

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import com.greenicephoenix.traceledger.domain.model.AccountType

class AccountsViewModel : ViewModel() {

    // Internal mutable state
    private val _accounts = MutableStateFlow(
        listOf(
            AccountUiModel(
                id = "1",
                name = "Main Bank",
                balance = "₹84,500.00",
                type = AccountType.BANK,
                includeInTotal = true,
                color = 0xFF4CAF50
            ),
            AccountUiModel(
                id = "2",
                name = "Cash Wallet",
                balance = "₹2,000.00",
                type = AccountType.CASH,
                includeInTotal = true,
                color = 0xFFFF5722
            ),
            AccountUiModel(
                id = "3",
                name = "ICICI",
                balance = "₹50.00",
                type = AccountType.BANK,
                includeInTotal = true,
                color = 0xFF03A9F4
            )
        )
    )

    // Public immutable state
    val accounts: StateFlow<List<AccountUiModel>> = _accounts.asStateFlow()

    /**
     * Adds a new account.
     * (Later this will persist to Room)
     */
    fun addAccount(account: AccountUiModel) {
        _accounts.value = _accounts.value + account
    }

    /**
     * Updates an existing account.
     */
    fun updateAccount(updated: AccountUiModel) {
        _accounts.value = _accounts.value.map {
            if (it.id == updated.id) updated else it
        }
    }
}
