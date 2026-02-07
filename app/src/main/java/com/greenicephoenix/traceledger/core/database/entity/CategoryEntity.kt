package com.greenicephoenix.traceledger.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["type"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,   // EXPENSE / INCOME
    val color: Long,
    val icon: String
)
