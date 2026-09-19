package com.expenser.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.expenser.app.data.db.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT id FROM users LIMIT 1")
    suspend fun firstUserId(): String?

    @Insert
    suspend fun insert(user: UserEntity)
}
