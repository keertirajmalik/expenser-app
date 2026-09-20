package com.expenser.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name COLLATE NOCASE")
    fun observeByType(type: EntryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: String): CategoryEntity?

    /** How many transactions reference this category. Gates changing its [EntryType]. */
    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :id")
    suspend fun transactionCount(id: String): Int

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    /** Throws SQLiteConstraintException if the category is referenced by a transaction. */
    @Delete
    suspend fun delete(category: CategoryEntity)

    /** One-shot snapshot for backup export. */
    @Query("SELECT * FROM categories")
    suspend fun getAll(): List<CategoryEntity>

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
