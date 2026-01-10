package com.greenicephoenix.traceledger.domain.model

import java.time.Instant
import java.time.LocalDate
import java.math.BigDecimal

/**
 * UI / domain representation of a transaction.
 *
 * Rules:
 * - amount is always positive
 * - account direction is defined by [type]
 * - category applies only to EXPENSE and INCOME
 */
data class TransactionUiModel(
    val id: String,

    // Core
    val type: TransactionType,
    val amount: BigDecimal,      // always positive
    val date: LocalDate,         // user-selected date

    // Accounts
    val fromAccountId: String?,  // EXPENSE, TRANSFER
    val toAccountId: String?,    // INCOME, TRANSFER

    // Category
    val categoryId: String?,     // EXPENSE, INCOME only

    // Optional metadata
    val note: String? = null,

    // System
    val createdAt: Instant
)
