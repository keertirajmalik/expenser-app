package com.expenser.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expenser.app.data.model.EntryType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("categoryId"), Index("date"), Index("type")],
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Money in minor units (e.g. 1250 = 12.50). Never a float. */
    val amountMinor: Long,
    val categoryId: String,
    /** ISO date, yyyy-MM-dd. */
    val date: String,
    val note: String?,
    val type: EntryType,
    val userId: String,
)
