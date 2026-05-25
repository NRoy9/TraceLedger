package com.greenicephoenix.traceledger.feature.categories.data

import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity

object CategorySeed {

    fun defaults(): List<CategoryEntity> = listOf(

        // ──────────── EXPENSE ────────────────────────────────────────────────
        CategoryEntity(id = "exp_food",          name = "Food",           type = "EXPENSE", color = 0xFFE53935, icon = "food"),
        CategoryEntity(id = "exp_groceries",     name = "Groceries",      type = "EXPENSE", color = 0xFFD32F2F, icon = "groceries"),
        CategoryEntity(id = "exp_transport",     name = "Transport",      type = "EXPENSE", color = 0xFFD81B60, icon = "transport"),
        CategoryEntity(id = "exp_fuel",          name = "Fuel",           type = "EXPENSE", color = 0xFF6D4C41, icon = "fuel"),
        CategoryEntity(id = "exp_shopping",      name = "Shopping",       type = "EXPENSE", color = 0xFF8E24AA, icon = "shopping"),
        CategoryEntity(id = "exp_bills",         name = "Bills",          type = "EXPENSE", color = 0xFF5E35B1, icon = "bills"),
        CategoryEntity(id = "exp_rent",          name = "Rent",           type = "EXPENSE", color = 0xFF7E57C2, icon = "rent"),
        CategoryEntity(id = "exp_healthcare",    name = "Healthcare",     type = "EXPENSE", color = 0xFF3949AB, icon = "healthcare"),
        CategoryEntity(id = "exp_insurance",     name = "Insurance",      type = "EXPENSE", color = 0xFF1E88E5, icon = "insurance"),
        CategoryEntity(id = "exp_entertainment", name = "Entertainment",  type = "EXPENSE", color = 0xFF039BE5, icon = "entertainment"),
        CategoryEntity(id = "exp_subscription",  name = "Subscriptions",  type = "EXPENSE", color = 0xFF00897B, icon = "subscription"),
        CategoryEntity(id = "exp_travel",        name = "Travel",         type = "EXPENSE", color = 0xFF00897B, icon = "travel"),
        CategoryEntity(id = "exp_education",     name = "Education",      type = "EXPENSE", color = 0xFF6D4C41, icon = "education"),
        CategoryEntity(id = "exp_gifts",         name = "Gifts",          type = "EXPENSE", color = 0xFFEC407A, icon = "gift"),
        CategoryEntity(id = "exp_personal",      name = "Personal Care",  type = "EXPENSE", color = 0xFF8D6E63, icon = "personal_care"),
        CategoryEntity(id = "exp_pets",          name = "Pets",           type = "EXPENSE", color = 0xFFFF7043, icon = "pets"),
        CategoryEntity(id = "exp_other",         name = "Other",          type = "EXPENSE", color = 0xFF546E7A, icon = "other"),

        // ──────────── INCOME ─────────────────────────────────────────────────
        CategoryEntity(id = "inc_salary",         name = "Salary",         type = "INCOME", color = 0xFF2E7D32, icon = "salary"),
        CategoryEntity(id = "inc_account_credit", name = "Account Credit", type = "INCOME", color = 0xFF388E3C, icon = "account_credit"),
        CategoryEntity(id = "inc_interest",       name = "Interest",       type = "INCOME", color = 0xFF00695C, icon = "interest"),
        CategoryEntity(id = "inc_gift",           name = "Gift",           type = "INCOME", color = 0xFF8E24AA, icon = "gift"),
        CategoryEntity(id = "inc_refund",         name = "Refund",         type = "INCOME", color = 0xFF039BE5, icon = "refund"),
        CategoryEntity(id = "inc_other",          name = "Other",          type = "INCOME", color = 0xFF546E7A, icon = "other"),

        // ──────────── INVESTMENT ─────────────────────────────────────────────
        CategoryEntity(id = "inv_stocks",        name = "Stocks",        type = "INVESTMENT", color = 0xFFF9A825, icon = "stocks"),
        CategoryEntity(id = "inv_mutual_funds",  name = "Mutual Funds",  type = "INVESTMENT", color = 0xFFF57F17, icon = "mutual_funds"),
        CategoryEntity(id = "inv_fd",            name = "Fixed Deposit", type = "INVESTMENT", color = 0xFFE65100, icon = "fd"),
        CategoryEntity(id = "inv_gold",          name = "Gold",          type = "INVESTMENT", color = 0xFFFFB300, icon = "gold"),
        CategoryEntity(id = "inv_crypto",        name = "Crypto",        type = "INVESTMENT", color = 0xFF6D4C41, icon = "crypto"),
        CategoryEntity(id = "inv_real_estate",   name = "Real Estate",   type = "INVESTMENT", color = 0xFF4527A0, icon = "real_estate"),
        CategoryEntity(id = "inv_retirement",    name = "Retirement",    type = "INVESTMENT", color = 0xFF558B2F, icon = "retirement"),
        CategoryEntity(id = "inv_other",         name = "Other",         type = "INVESTMENT", color = 0xFF546E7A, icon = "other"),
        )
}