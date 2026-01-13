package com.greenicephoenix.traceledger.core.repository

import com.greenicephoenix.traceledger.core.database.dao.AccountDao
import com.greenicephoenix.traceledger.core.database.entity.AccountEntity
import com.greenicephoenix.traceledger.domain.model.AccountType
import com.greenicephoenix.traceledger.domain.model.AccountUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val accountDao: AccountDao
) {

    /** Observe all accounts as UI models */
    fun observeAccounts(): Flow<List<AccountUiModel>> {
        return accountDao.observeAllAccounts()
            .map { entities ->
                entities.map { it.toUiModel() }
            }
    }

    /** Insert or update account */
    suspend fun upsert(account: AccountUiModel) {
        accountDao.upsertAccount(account.toEntity())
    }


    /** Delete account */
    suspend fun delete(accountId: String) {
        accountDao.deleteAccount(accountId)
    }

    // ---------------- MAPPERS ----------------

    private fun AccountEntity.toUiModel(): AccountUiModel {
        return AccountUiModel(
            id = id,
            name = name,
            balance = balance,
            type = AccountType.valueOf(type),
            includeInTotal = includeInTotal,
            details = details,
            color = color
        )
    }

    private fun AccountUiModel.toEntity(): AccountEntity {
        return AccountEntity(
            id = id,
            name = name,
            balance = balance,
            type = type.name,
            includeInTotal = includeInTotal,
            details = details,
            color = color
        )
    }
}