package com.greenicephoenix.traceledger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,

    val type: String,           // TransactionType.name

    val amount: BigDecimal,

    val date: LocalDate,

    val fromAccountId: String?,

    val toAccountId: String?,

    val categoryId: String?,

    val note: String?,

    val createdAt: Instant
)