package com.expenser.app.data.repo

import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val currentUserId: suspend () -> String,
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeByType(type: EntryType): Flow<List<CategoryEntity>> =
        categoryDao.observeByType(type)

    /** Create (id == null) or update an existing category. */
    suspend fun save(
        id: String?,
        name: String,
        type: EntryType,
        description: String?,
    ) {
        categoryDao.upsert(
            CategoryEntity(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                type = type,
                description = description?.trim()?.ifBlank { null },
                userId = currentUserId(),
            ),
        )
    }

    /** Throws SQLiteConstraintException if the category is still referenced. */
    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
}
