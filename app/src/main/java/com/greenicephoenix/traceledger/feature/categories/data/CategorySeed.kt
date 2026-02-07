package com.greenicephoenix.traceledger.feature.categories.data

import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity

object CategorySeed {

    fun defaults(): List<CategoryEntity> = listOf(

        // -------- EXPENSE --------
        CategoryEntity(
            id = "exp_food",
            name = "Food",
            type = "EXPENSE",
            color = 0xFFE53935,
            icon = "food"
        ),
        CategoryEntity(
            id = "exp_transport",
            name = "Transport",
            type = "EXPENSE",
            color = 0xFFD81B60,
            icon = "transport"
        ),
        CategoryEntity(
            id = "exp_shopping",
            name = "Shopping",
            type = "EXPENSE",
            color = 0xFF8E24AA,
            icon = "shopping"
        ),
        CategoryEntity(
            id = "exp_bills",
            name = "Bills",
            type = "EXPENSE",
            color = 0xFF5E35B1,
            icon = "bills"
        ),
        CategoryEntity(
            id = "exp_healthcare",
            name = "Healthcare",
            type = "EXPENSE",
            color = 0xFF3949AB,
            icon = "healthcare"
        ),
        CategoryEntity(
            id = "exp_entertainment",
            name = "Entertainment",
            type = "EXPENSE",
            color = 0xFF1E88E5,
            icon = "entertainment"
        ),

        // -------- INCOME --------
        CategoryEntity(
            id = "inc_salary",
            name = "Salary",
            type = "INCOME",
            color = 0xFF2E7D32,
            icon = "salary"
        ),
        CategoryEntity(
            id = "inc_account_credit",
            name = "Account Credit",
            type = "INCOME",
            color = 0xFF388E3C,
            icon = "account_credit"
        ),
        CategoryEntity(
            id = "inc_interest",
            name = "Interest",
            type = "INCOME",
            color = 0xFF00695C,
            icon = "interest"
        )
    )
}