package com.greenicephoenix.traceledger.core.export

import kotlinx.serialization.Serializable

@Serializable
data class ExportEnvelope(
    val meta: ExportMeta,
    val accounts: List<AccountExport>,
    val categories: List<CategoryExport>,
    val budgets: List<BudgetExport>,
    val transactions: List<TransactionExport>
)

@Serializable
data class ExportMeta(
    val app: String,
    val appVersion: String,
    val schemaVersion: Int,
    val exportedAtIso: String
)

@Serializable
data class AccountExport(
    val id: String,
    val name: String,
    val type: String,
    val balance: String,
    val color: Long,
    val includeInTotal: Boolean
)

@Serializable
data class CategoryExport(
    val id: String,
    val name: String,
    val type: String,
    val color: Long,
    val icon: String
)

@Serializable
data class BudgetExport(
    val id: String,
    val categoryId: String,
    val limitAmount: String,
    val month: String,
    val isActive: Boolean
)

@Serializable
data class TransactionExport(
    val id: String,
    val type: String,
    val amount: String,
    val date: String,
    val fromAccountId: String?,
    val toAccountId: String?,
    val categoryId: String?,
    val note: String?,
    val createdAtEpoch: Long
)