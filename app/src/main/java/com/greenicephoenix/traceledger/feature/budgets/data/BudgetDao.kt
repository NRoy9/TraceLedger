package com.greenicephoenix.traceledger.feature.budgets.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.greenicephoenix.traceledger.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun observeBudgetsForMonth(month: YearMonth): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteBudget(budgetId: String)

    @Query("SELECT * FROM budgets")
    suspend fun getAllOnce(): List<BudgetEntity>

    @Insert
    suspend fun insert(entity: BudgetEntity)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

}