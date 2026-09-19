package com.expenser.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String,
    /** Local file path to the avatar image, or null for the initials fallback. */
    val image: String? = null,
)
