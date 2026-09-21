package com.expenser.app.data.repo

import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * A save that would break a domain rule. Its [message] is written for the user, so
 * unlike an arbitrary throwable it is safe to show verbatim.
 */
class CategoryRuleViolation(message: String) : Exception(message)

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val currentUserId: suspend () -> String,
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    fun observeByType(type: EntryType): Flow<List<CategoryEntity>> =
        categoryDao.observeByType(type)

    /**
     * Create (id == null) or update an existing category.
     *
     * @throws CategoryRuleViolation if this would retype a category that transactions
     * already use. [EntryType] is the discriminator that decides which transactions may
     * pick a category, so retyping one silently strands its existing rows: they keep the
     * old type and still list under it, but that list's category picker no longer offers
     * them, leaving them uneditable.
     */
    suspend fun save(
        id: String?,
        name: String,
        type: EntryType,
        description: String?,
    ) {
        if (id != null) {
            val existing = categoryDao.findById(id)
            if (existing != null && existing.type != type && categoryDao.transactionCount(id) > 0) {
                throw CategoryRuleViolation(
                    "\"${existing.name}\" already has ${existing.type.name.lowercase()} " +
                        "transactions, so its type can't be changed.",
                )
            }
        }
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
