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

/** Sum of one category's transactions (from an aggregate query). */
data class CategoryTotal(
    val categoryId: String,
    val totalMinor: Long,
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

    /** Every transaction (all types) with its category name; scoped/aggregated in Kotlin. */
    @Query(
        """
        SELECT t.*, c.name AS categoryName
        FROM transactions t
        JOIN categories c ON c.id = t.categoryId
        ORDER BY t.date DESC, t.name COLLATE NOCASE
        """,
    )
    fun observeAllWithCategory(): Flow<List<TransactionListItem>>

    /**
     * Total per category over an inclusive ISO date range; a null bound is open. Reads
     * only `transactions`, so editing a category doesn't re-run it, and returns one row
     * per category instead of every transaction.
     */
    @Query(
        """
        SELECT categoryId, SUM(amountMinor) AS totalMinor
        FROM transactions
        WHERE (:from IS NULL OR date >= :from) AND (:to IS NULL OR date <= :to)
        GROUP BY categoryId
        """,
    )
    fun observeTotalsByCategory(from: String?, to: String?): Flow<List<CategoryTotal>>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    /** One-shot snapshot for backup export. */
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Upsert
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
