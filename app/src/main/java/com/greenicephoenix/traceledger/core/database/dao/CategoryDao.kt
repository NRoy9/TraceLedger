package com.greenicephoenix.traceledger.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.greenicephoenix.traceledger.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: CategoryEntity)

    // UPDATE-only — use for renaming/editing existing categories.
    // Does NOT delete-then-insert, so FK constraints on transactions are safe.
    @Query("""
        UPDATE categories 
        SET name = :name, color = :color, icon = :icon 
        WHERE id = :id
    """)
    suspend fun update(id: String, name: String, color: Long, icon: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun delete(categoryId: String)

    @Query("SELECT * FROM categories")
    suspend fun getAllOnce(): List<CategoryEntity>

    @Insert
    suspend fun insert(entity: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): CategoryEntity?
}
