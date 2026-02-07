package com.greenicephoenix.traceledger.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity
import com.greenicephoenix.traceledger.core.repository.CategoryRepository
import kotlinx.coroutines.flow.StateFlow
import com.greenicephoenix.traceledger.domain.model.CategoryUiModel
import com.greenicephoenix.traceledger.domain.model.CategoryType
import com.greenicephoenix.traceledger.feature.categories.data.CategorySeed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryUiModel>> =
        repository.observeCategories()
            .map { entities ->
                entities.map {
                    CategoryUiModel(
                        id = it.id,
                        name = it.name,
                        type = CategoryType.valueOf(it.type),
                        color = it.color,
                        icon = it.icon
                    )
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    init {
        viewModelScope.launch {
            repository.seedIfEmpty(CategorySeed.defaults())
        }
    }

    fun addCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            repository.upsert(category.toEntity())
        }
    }

    fun updateCategory(category: CategoryUiModel) {
        viewModelScope.launch {
            repository.upsert(category.toEntity())
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.delete(categoryId)
        }
    }

    private fun CategoryUiModel.toEntity(): CategoryEntity =
        CategoryEntity(
            id = id,
            name = name,
            type = type.name,
            color = color,
            icon = icon
        )
}