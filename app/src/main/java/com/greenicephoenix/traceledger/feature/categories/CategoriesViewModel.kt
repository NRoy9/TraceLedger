package com.greenicephoenix.traceledger.feature.categories

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryType

class CategoriesViewModel : ViewModel() {

    private val _categories = MutableStateFlow(
        listOf(
            // -------- EXPENSE DEFAULTS --------
            CategoryUiModel(
                id = "exp_food",
                name = "Food",
                type = CategoryType.EXPENSE,
                color = Color(0xFFE53935).toArgb().toLong(),
                icon = "food"
            ),
            CategoryUiModel(
                id = "exp_transport",
                name = "Transport",
                type = CategoryType.EXPENSE,
                color = Color(0xFFD81B60).toArgb().toLong(),
                icon = "transport"
            ),
            CategoryUiModel(
                id = "exp_shopping",
                name = "Shopping",
                type = CategoryType.EXPENSE,
                color = Color(0xFF8E24AA).toArgb().toLong(),
                icon = "shopping"
            ),
            CategoryUiModel(
                id = "exp_bills",
                name = "Bills",
                type = CategoryType.EXPENSE,
                color = Color(0xFF5E35B1).toArgb().toLong(),
                icon = "bills"
            ),
            CategoryUiModel(
                id = "exp_healthcare",
                name = "Healthcare",
                type = CategoryType.EXPENSE,
                color = Color(0xFF3949AB).toArgb().toLong(),
                icon = "healthcare"
            ),
            CategoryUiModel(
                id = "exp_entertainment",
                name = "Entertainment",
                type = CategoryType.EXPENSE,
                color = Color(0xFF1E88E5).toArgb().toLong(),
                icon = "entertainment"
            ),

// -------- INCOME DEFAULTS --------
            CategoryUiModel(
                id = "inc_salary",
                name = "Salary",
                type = CategoryType.INCOME,
                color = Color(0xFF2E7D32).toArgb().toLong(),
                icon = "salary"
            ),
            CategoryUiModel(
                id = "inc_account_credit",
                name = "Account Credit",
                type = CategoryType.INCOME,
                color = Color(0xFF388E3C).toArgb().toLong(),
                icon = "account_credit"
            ),
            CategoryUiModel(
                id = "inc_interest",
                name = "Interest",
                type = CategoryType.INCOME,
                color = Color(0xFF00695C).toArgb().toLong(),
                icon = "interest"
            )
        )
    )

    //val categories: StateFlow<List<CategoryUiModel>> = _categories.asStateFlow()
    val categories: StateFlow<List<CategoryUiModel>> = _categories

    // CRUD APIs (used later)
    fun addCategory(category: CategoryUiModel) {
        _categories.value = _categories.value + category
    }

    fun updateCategory(category: CategoryUiModel) {
        _categories.value = _categories.value.map {
            if (it.id == category.id) category else it
        }
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filterNot { it.id == categoryId }
    }
}