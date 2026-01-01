package com.greenicephoenix.traceledger.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments

/**
 * Used ONLY for sorting accounts on dashboard
 */
val AccountUiModel.numericBalance: Double
    get() = balance
        .replace("₹", "")
        .replace(",", "")
        .toDoubleOrNull()
        ?: 0.0

/**
 * Icon based on account type
 */
fun AccountType.icon() = when (this) {
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CASH -> Icons.Default.Payments
    AccountType.WALLET -> Icons.Default.AccountBalanceWallet
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
}

/**
 * Display label for UI
 */
val AccountType.displayName: String
    get() = name.replace("_", " ")
