package com.greenicephoenix.traceledger.core.repository

import com.greenicephoenix.traceledger.core.database.dao.CategoryDao
import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val dao: CategoryDao
) {

    fun observeCategories(): Flow<List<CategoryEntity>> =
        dao.observeAll()

    suspend fun upsert(category: CategoryEntity) =
        dao.upsert(category)

    suspend fun delete(categoryId: String) =
        dao.delete(categoryId)

    suspend fun seedIfEmpty(defaults: List<CategoryEntity>) {
        if (dao.count() == 0) {
            dao.insertAll(defaults)
        }
    }
}