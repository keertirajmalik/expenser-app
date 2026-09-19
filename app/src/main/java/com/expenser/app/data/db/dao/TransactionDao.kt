package com.expenser.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Query
import androidx.room.Upsert
import com.expenser.app.data.db.entity.TransactionEntity
import com.expenser.app.data.model.EntryType
import kotlinx.coroutines.flow.Flow

/** A transaction row plus its category's display name (from a join). */
data class TransactionListItem(
    @Embedded val transaction: TransactionEntity,
    val categoryName: String,
)

@Dao
interface TransactionDao {
    @Query(
        """
        SELECT t.*, c.name AS categoryName
        FROM transactions t
        JOIN categories c ON c.id = t.categoryId
        WHERE t.type = :type
        ORDER BY t.date DESC, t.name COLLATE NOCASE
        """,
    )
    fun observeByType(type: EntryType): Flow<List<TransactionListItem>>

    @Query("SELECT COALESCE(SUM(amountMinor), 0) FROM transactions WHERE type = :type")
    fun observeTotalMinor(type: EntryType): Flow<Long>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}
