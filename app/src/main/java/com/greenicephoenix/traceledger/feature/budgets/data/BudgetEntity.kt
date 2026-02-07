package com.greenicephoenix.traceledger.feature.budgets.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity
import java.math.BigDecimal
import java.time.YearMonth

/**
 * BudgetEntity
 *
 * Rules:
 * - Expense categories only
 * - Month-specific
 * - Read-only evaluator (does not affect transactions)
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index(value = ["categoryId", "month"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val limitAmount: BigDecimal,
    val month: YearMonth,
    val isActive: Boolean
)
