package com.expenser.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expenser.app.data.model.EntryType

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "userId"], unique = true), Index("name")],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: EntryType,
    val description: String?,
    val userId: String,
)
