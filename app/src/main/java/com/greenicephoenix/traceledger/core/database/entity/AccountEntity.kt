package com.greenicephoenix.traceledger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val balance: BigDecimal,
    val type: String,
    val includeInTotal: Boolean,
    val details: String?,
    val color: Long,
    val lastFourDigits: String? = null   // ← new, nullable, default null for existing rows
)