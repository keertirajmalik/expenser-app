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

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    /** Throws SQLiteConstraintException if the category is referenced by a transaction. */
    @Delete
    suspend fun delete(category: CategoryEntity)
}
