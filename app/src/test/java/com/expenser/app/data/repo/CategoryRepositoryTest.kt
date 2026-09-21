package com.expenser.app.data.repo

import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.entity.CategoryEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryRepositoryTest {

    /** In-memory CategoryDao: enough to exercise the retype rule without Room. */
    private class FakeCategoryDao(
        private val rows: MutableMap<String, CategoryEntity> = mutableMapOf(),
        var transactionCount: Int = 0,
    ) : CategoryDao {
        val upserted = mutableListOf<CategoryEntity>()

        override suspend fun findById(id: String) = rows[id]
        override suspend fun transactionCount(id: String) = transactionCount
        override suspend fun upsert(category: CategoryEntity) {
            rows[category.id] = category
            upserted += category
        }

        override fun observeAll(): Flow<List<CategoryEntity>> = flowOf(rows.values.toList())
        override fun observeByType(type: EntryType): Flow<List<CategoryEntity>> =
            flowOf(rows.values.filter { it.type == type })

        override suspend fun delete(category: CategoryEntity) { rows -= category.id }
        override suspend fun getAll() = rows.values.toList()
        override suspend fun upsertAll(categories: List<CategoryEntity>) {
            categories.forEach { rows[it.id] = it }
        }
        override suspend fun deleteAll() = rows.clear()
    }

    private val food = CategoryEntity("c1", "Food", EntryType.Expense, null, "u1")

    private fun repo(dao: CategoryDao) = CategoryRepository(dao) { "u1" }

    @Test
    fun `retyping a category that has transactions is rejected`() = runBlocking {
        val dao = FakeCategoryDao(mutableMapOf(food.id to food), transactionCount = 3)

        val e = assertThrows(CategoryRuleViolation::class.java) {
            runBlocking { repo(dao).save(food.id, "Food", EntryType.Income, null) }
        }

        assertTrue(e.message!!, e.message!!.contains("type can't be changed"))
        assertTrue("nothing should have been written", dao.upserted.isEmpty())
    }

    @Test
    fun `retyping an unused category is allowed`() = runBlocking {
        val dao = FakeCategoryDao(mutableMapOf(food.id to food), transactionCount = 0)

        repo(dao).save(food.id, "Food", EntryType.Income, null)

        assertEquals(EntryType.Income, dao.upserted.single().type)
    }

    @Test
    fun `renaming a used category is allowed while its type is unchanged`() = runBlocking {
        val dao = FakeCategoryDao(mutableMapOf(food.id to food), transactionCount = 3)

        repo(dao).save(food.id, "  Groceries  ", EntryType.Expense, "  dining  ")

        val saved = dao.upserted.single()
        assertEquals("Groceries", saved.name)
        assertEquals("dining", saved.description)
    }

    @Test
    fun `creating a category never consults the retype rule`() = runBlocking {
        val dao = FakeCategoryDao(transactionCount = 99)

        repo(dao).save(null, "New", EntryType.Investment, null)

        assertEquals(EntryType.Investment, dao.upserted.single().type)
    }

    @Test
    fun `a blank description is stored as null`() = runBlocking {
        val dao = FakeCategoryDao()

        repo(dao).save(null, "New", EntryType.Expense, "   ")

        assertEquals(null, dao.upserted.single().description)
    }
}
