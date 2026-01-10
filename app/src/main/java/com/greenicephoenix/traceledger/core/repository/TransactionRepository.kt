package com.greenicephoenix.traceledger.core.repository

import com.greenicephoenix.traceledger.core.database.dao.TransactionDao
import com.greenicephoenix.traceledger.core.database.entity.TransactionEntity
import com.greenicephoenix.traceledger.domain.model.TransactionType
import com.greenicephoenix.traceledger.domain.model.TransactionUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao
) {

    /** Observe all transactions as UI models */
    fun observeTransactions(): Flow<List<TransactionUiModel>> {
        return transactionDao.observeAllTransactions()
            .map { entities ->
                entities.map { it.toUiModel() }
            }
    }

    /** Insert transaction */
    suspend fun insert(transaction: TransactionUiModel) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    /** Delete transaction */
    suspend fun delete(transactionId: String) {
        transactionDao.deleteTransaction(transactionId)
    }

    /** Get a single transaction by id */
    suspend fun getTransactionById(transactionId: String): TransactionUiModel? {
        return transactionDao
            .getTransactionById(transactionId)
            ?.toUiModel()
    }

    /** Update an existing transaction */
    suspend fun update(transaction: TransactionUiModel) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    // ---------------- MAPPERS ----------------

    private fun TransactionEntity.toUiModel(): TransactionUiModel {
        return TransactionUiModel(
            id = id,
            type = TransactionType.valueOf(type),
            amount = amount,
            date = date,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            categoryId = categoryId,
            note = note,
            createdAt = createdAt
        )
    }

    private fun TransactionUiModel.toEntity(): TransactionEntity {
        return TransactionEntity(
            id = id,
            type = type.name,
            amount = amount,
            date = date,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            categoryId = categoryId,
            note = note,
            createdAt = createdAt
        )
    }
}