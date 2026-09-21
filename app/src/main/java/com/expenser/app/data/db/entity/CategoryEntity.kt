package com.expenser.app.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.expenser.app.data.model.EntryType

/**
 * The composite index carries the uniqueness constraint and is the only one needed.
 *
 * A second `Index("name")` used to sit beside it and served nothing. No query filters on
 * `name` - the two that mention it sort by `name COLLATE NOCASE`, and an index declared
 * with the default BINARY collation cannot satisfy a NOCASE ordering, so both planned as
 * a table scan plus a temp B-tree with the index present. Were a `WHERE name = ?` ever
 * added, the composite index serves it anyway as a leftmost-prefix match.
 */
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name", "userId"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: EntryType,
    val description: String?,
    val userId: String,
)
