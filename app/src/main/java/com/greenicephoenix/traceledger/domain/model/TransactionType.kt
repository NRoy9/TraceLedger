package com.greenicephoenix.traceledger.domain.model

/**
 * Type of financial transaction.
 *
 * EXPENSE  → money leaves an account
 * INCOME   → money enters an account
 * TRANSFER → money moves between accounts
 */
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}
