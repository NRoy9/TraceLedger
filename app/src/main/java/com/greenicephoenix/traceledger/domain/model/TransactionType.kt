package com.greenicephoenix.traceledger.domain.model

/**
 * Type of financial transaction.
 *
 * EXPENSE    → money leaves an account (reduces balance)
 * INCOME     → money enters an account (increases balance)
 * TRANSFER   → money moves between accounts
 * INVESTMENT → money leaves an account for investment purposes (reduces balance like EXPENSE,
 *              but tracked separately from regular expenses)
 */
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    INVESTMENT
}