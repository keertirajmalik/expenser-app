package com.expenser.app.data.repo

import androidx.room.withTransaction
import com.expenser.app.data.backup.BackupData
import com.expenser.app.data.db.ExpenserDatabase
import com.expenser.app.data.db.dao.CategoryDao
import com.expenser.app.data.db.dao.TransactionDao

/**
 * Exports the whole database to a [BackupData] snapshot and restores one back.
 * Restore is a full replace (recover onto a fresh install after data loss), run
 * in a single transaction so a failure can't leave a half-written database.
 */
class BackupRepository(
    private val db: ExpenserDatabase,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val currentUserId: suspend () -> String,
) {
    suspend fun export(): BackupData =
        BackupData(
            categories = categoryDao.getAll(),
            transactions = transactionDao.getAll(),
        )

    suspend fun restore(data: BackupData) {
        val categoryIds = data.categories.mapTo(HashSet()) { it.id }
        val danglingCategoryId = data.transactions.map { it.categoryId }.firstOrNull { it !in categoryIds }
        require(danglingCategoryId == null) {
            "Backup references category '$danglingCategoryId', which isn't in the backup."
        }

        // Re-home imported rows onto this device's local user so a backup from
        // another install still lines up with the seeded user.
        val userId = currentUserId()
        db.withTransaction {
            transactionDao.deleteAll()
            categoryDao.deleteAll()
            categoryDao.upsertAll(data.categories.map { it.copy(userId = userId) })
            transactionDao.upsertAll(data.transactions.map { it.copy(userId = userId) })
        }
    }
}
