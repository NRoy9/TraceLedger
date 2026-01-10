package com.greenicephoenix.traceledger.domain.model

data class CategoryUiModel(
    val id: String,          // stable, UUID or timestamp-based
    val name: String,        // user-defined, editable
    val type: CategoryType,  // EXPENSE or INCOME
    val color: Long,         // ARGB, same storage rule as AccountUiModel
    val icon: String         // icon identifier (NOT resource id)
)