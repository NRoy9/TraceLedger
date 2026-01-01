package com.greenicephoenix.traceledger.feature.accounts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.greenicephoenix.traceledger.domain.model.AccountType

fun accountTypeIcon(type: AccountType): ImageVector {
    return when (type) {
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.WALLET -> Icons.Default.Wallet
        AccountType.CASH -> Icons.Default.Payments
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
    }
}
