package com.greenicephoenix.traceledger.core.export

import com.greenicephoenix.traceledger.core.database.entity.*
import com.greenicephoenix.traceledger.feature.budgets.data.BudgetEntity

internal fun AccountEntity.toExport() = AccountExport(
    id = id,
    name = name,
    type = type,
    balance = balance.toPlainString(),
    color = color,
    includeInTotal = includeInTotal
)

internal fun CategoryEntity.toExport() = CategoryExport(
    id = id,
    name = name,
    type = type,
    color = color,
    icon = icon
)

internal fun BudgetEntity.toExport() = BudgetExport(
    id = id,
    categoryId = categoryId,
    limitAmount = limitAmount.toPlainString(),
    month = month.toString(),
    isActive = isActive
)

internal fun TransactionEntity.toExport() = TransactionExport(
    id = id,
    type = type,
    amount = amount.toPlainString(),
    date = date.toString(),
    fromAccountId = fromAccountId,
    toAccountId = toAccountId,
    categoryId = categoryId,
    note = note,
    createdAtEpoch = createdAt.epochSecond
)