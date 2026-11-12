package com.expenser.app.data.repo

import com.expenser.app.data.db.dao.TransactionDao
import com.expenser.app.data.db.dao.TransactionListItem
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.util.UUID

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val currentUserId: suspend () -> String,
) {
    fun observeByType(type: EntryType): Flow<List<TransactionListItem>> =
        transactionDao.observeByType(type)

    fun observeAllWithCategory(): Flow<List<TransactionListItem>> =
        transactionDao.observeAllWithCategory()

    /** Total per category id within [month]; a null month means all time. */
    fun observeTotalsByCategory(month: YearMonth?): Flow<Map<String, Long>> =
        transactionDao.observeTotalsByCategory(month?.atDay(1)?.toString(), month?.atEndOfMonth()?.toString())
            .map { totals -> totals.associate { it.categoryId to it.totalMinor } }

    /** Create (id == null) or update an existing transaction. */
    suspend fun save(
        id: String?,
        name: String,
        amountMinor: Long,
        categoryId: String,
        date: String,
        note: String?,
        type: EntryType,
    ) {
        transactionDao.upsert(
            TransactionEntity(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                amountMinor = amountMinor,
                categoryId = categoryId,
                date = date,
                note = note?.trim()?.ifBlank { null },
                type = type,
                userId = currentUserId(),
            ),
        )
    }

    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)

    /** Re-insert a previously deleted transaction (for undo). */
    suspend fun restore(transaction: TransactionEntity) = transactionDao.upsert(transaction)
}
